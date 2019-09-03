#!/usr/bin/perl

# Insert SemRep predications to the Semantic Medline db.
# Input file should have full-fielded output format (-F).
# dbname is currently semmed2006.

use DBI;

$dbname = shift @ARGV;
chomp $dbname;
$input_file = shift @ARGV;
chomp $input_file;
$account = shift @ARGV;
chomp $account;
$password = shift @ARGV;
chomp $password;
$type = "semrep";

$dbh = DBI->connect("dbi:mysql:database=" . $dbname . ";host=indsrv2:3306;user=" . $account  .  ";password="  . $password) or die "Couldn't connect to database: " . DBI->errstr;
$insert_sentence_sth = $dbh->prepare_cached('INSERT INTO SENTENCE (PMID,TYPE,NUMBER,SENT_START_INDEX, SENT_END_INDEX, SECTION_HEADER,SENTENCE) VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE SENTENCE=?') or die "Couldn't prepare statement: " . $dbh->errstr;
$insert_entity_sth = $dbh->prepare_cached('INSERT INTO ENTITY (SENTENCE_ID, CUI, NAME, SEMTYPE, GENE_ID, GENE_NAME, TEXT, SCORE, START_INDEX, END_INDEX) VALUES(?,?,?,?,?,?,?,?,?,?)') or die "Couldn't prepare statement: " . $dbh->errstr;

$insert_predication_sth = $dbh->prepare('INSERT INTO PREDICATION (SENTENCE_ID, PMID, PREDICATE, SUBJECT_CUI, SUBJECT_NAME, SUBJECT_SEMTYPE, SUBJECT_NOVELTY, OBJECT_CUI,OBJECT_NAME, OBJECT_SEMTYPE, OBJECT_NOVELTY) VALUES (?,?,?,?,?,?,?,?,?,?,?)');
$insert_predication_aux_sth = $dbh->prepare_cached('INSERT INTO PREDICATION_AUX (PREDICATION_ID, SUBJECT_DIST, SUBJECT_MAXDIST, SUBJECT_START_INDEX, SUBJECT_END_INDEX, SUBJECT_TEXT, SUBJECT_SCORE, INDICATOR_TYPE, PREDICATE_START_INDEX, PREDICATE_END_INDEX, OBJECT_DIST, OBJECT_MAXDIST, OBJECT_START_INDEX, OBJECT_END_INDEX, OBJECT_TEXT, OBJECT_SCORE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)') or die  "Couldn't prepare statement: " . $dbh->errstr;
$select_sentence_sth = $dbh->prepare_cached('SELECT SENTENCE_ID FROM SENTENCE WHERE PMID=? AND TYPE=? AND NUMBER=?') or die "Couldn't prepare statement: " . $dbh->errstr;
$delete_sentence_sth = $dbh->prepare_cached('DELETE P FROM PREDICATION P, PREDICATION_AUX PA, SENTENCE S WHERE S.PMID=?  AND P.PREDICATION_ID=PA.PREDICATION_ID AND S.SENTENCE_ID=P.SENTENCE_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$delete_predication_sth = $dbh->prepare_cached('DELETE P FROM PREDICATION P WHERE P.SENTENCE_ID=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$delete_entity_sth = $dbh->prepare_cached('DELETE E FROM ENTITY E WHERE E.SENTENCE_ID=?') or die "Couldn't prepare statement: " . $dbh->errstr;

# $select_pmid_sentence_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM SENTENCE WHERE PMID = ?') or die "Couldn't prepare statement: " . $dbh->errstr;
$select_novelty_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM GENERIC_CONCEPT WHERE CUI = ?') or die "Couldn't prepare statement: " . $dbh->errstr;

open(F,"<:utf8",$input_file) or die("Can not open the $input_file file!");
# needed for unicode characters
$dbh->do('SET NAMES utf8');

$num_sentetce_pmid = 0;
while (<F>) {
    my($line) = $_;
    chomp($line);
    @line_elements = split(/\|/, $line);
    if ($line_elements[0] eq "SE") {
      $pmid = $line_elements[1];
      $senttype = $line_elements[3];
      $number = $line_elements[4];
      # ensure that if predications already exist in the database for the pmid, they are removed, as we are probably trying to update the predications
      # unless ($prev_pmid eq $pmid) {
	# $delete_predication_sth->execute($pmid,$type) or die "Couldn't execute statement: " + $delete_predication_sth->errstr;
      # }      	# get the sentence_id in the db for the given sentence.
      	
	$select_sentence_sth->execute($pmid,$senttype,$number) or die "Couldn't execute statement: " + $select_sentence_sth->errstr;
	
        $sentence_id = $select_sentence_sth->fetchrow_array();


      # we are looking at the text line. Insert or update the sentence record.
      if ($line_elements[5] eq "text") {	$sent_start_index = $line_elements[6];
	$sent_end_index = $line_elements[7];
	$sentence = $line_elements[8];
	$section_header = $line_elements[2];
	# print "SECTION HEADER : $section_header\n"; 
        
        if($sentence_id) {
        	
        	$delete_predication_sth->execute($sentence_id)  or die "Couldn't execute statement: " . $sth->errstr;
        	
        	# $delete_entity_sth->execute($sentence_id)  or die "Couldn't execute statement: " . $sth->errstr;
        
        }

	# print "$pmid\n";
	# $insert_sentence_sth->execute($pmid,$senttype,$number,$sent_start_index,$sent_end_index,$section_header,$sentence,$sentence) or die "Couldn't execute statement: " . $insert_sentence_sth->errstr;
	$insert_sentence_sth->execute($pmid,$senttype,$number,$sent_start_index,$sent_end_index,$section_header,$sentence,$sentence) 
      }
      # Version 3.0 handles the entities as well as predication      
      # elsif ($line_elements[5] eq "entity") {
      	# $cui = $line_elements[6];
      	# $meta_name = $line_elements[7];
      	# $semtype = $line_elements[8];
      	# $entrez_id = $line_elements[9];
      	# $entrez_name = $line_elements[10];
      	# $text = $line_elements[11];
      	# $score = $line_elements[15];
      	# $start_index = $line_elements[16];
	# $end_index = $line_elements[17];
	# if($sentence_id) {
		# $insert_entity_sth->execute($sentence_id, $cui, $meta_name, $semtype, $entrez_id, $entrez_name, $text, $score, $start_index, $end_index) or die "Couldn't execute statement: " . $sth->errstr;
	# }
      # }

      elsif ($line_elements[5] eq "relation") {
	# print "$line\n";
	undef $subj_text;
	undef $obj_text;
	undef $obj_ui;
	undef $obj_semtype;
	undef $subj_semtype;
	undef $subj_concsem_id;
	undef $obj_concsem_id;
	$subj_maxdist = $line_elements[6];
	$subj_dist = $line_elements[7];
	$subj_cui = $line_elements[8];
	$subj_meta_name = $line_elements[9];
	@subj_semtypes = split(/,/, $line_elements[10]);
	$subj_semtype = $line_elements[11];
	$subj_entrez_id = $line_elements[12];
	$subj_entrez_name = $line_elements[13];
	$subj_text = $line_elements[14];
	$subj_score = $line_elements[18];
	$subj_start_ind = $line_elements[19];
	$subj_end_ind = $line_elements[20];
	# read predicate data
	$indicator_type= $line_elements[21];
	$predicate = $line_elements[22];
	$neg = $line_elements[23];
	$pred_start_ind = $line_elements[24];
	$pred_end_ind = $line_elements[25];

	# take care of INFER and SPEC relations
	if ($predicate =~ /\(/) {
	  $predicate =~ s/\(.+\)//g;
	}

	if ($neg eq "negation") {
	  $predicate = "NEG_" . $predicate;
	}

	# read object
	$obj_maxdist = $line_elements[26];
	$obj_dist = $line_elements[27];
	$obj_cui = $line_elements[28];
	$obj_meta_name = $line_elements[29];
	@obj_semtypes = split(/,/, $line_elements[30]);
	$obj_semtype = $line_elements[31];
	$obj_entrez_id = $line_elements[32];
	$obj_entrez_name = $line_elements[33];
	$obj_text = $line_elements[34];
	$obj_score = $line_elements[38];
	$obj_start_ind = $line_elements[39];
	$obj_end_ind = $line_elements[40];
	# print "Subj: $subj_maxdist | $subj_dist | $subj_start_ind | $subj_end_ind | $subj_score | $subj_text\n";
	# print "Pred: $predicate | $indicator_type | $pred_start_ind | $pred_end_ind\n";
	# print "Obj: $obj_maxdist | $obj_dist | $obj_start_ind | $obj_end_ind | $obj_score | $obj_text\n";
	  # print "Pred number: $pred_number\n";
	  
	  # insert the predication 
	  $subj_entrez_id =~ s/\,/\|/g;
	  # print "subj_entrez_id: $subj_entrez_id\n";
	  $obj_entrez_id =~ s/\,/\|/g;
	  # print "obj_entrez_id: $obj_entrez_id\n";
	  if ( $subj_cui ne "" && $subj_entrez_id ne "") {
	  	$subj_id_list = $subj_cui . "|" . $subj_entrez_id;
	  } elsif ( $subj_cui ne "" && $subj_entrez_id eq "") {
	  	$subj_id_list = $subj_cui;
	  } elsif ( $subj_cui eq "" && $subj_entrez_id ne "") {
	  	$subj_id_list = $subj_entrez_id;
	  }
	  
	  if ($obj_cui ne "" && $obj_entrez_id ne "") {
	  	$obj_id_list = $obj_cui . "|" . $obj_entrez_id;
	  } elsif($obj_cui ne "" && $obj_entrez_id eq "")  {
	  	$obj_id_list = $obj_cui;
	  } elsif($obj_cui eq "" && $obj_entrez_id ne "")  {
	  	$obj_id_list = $obj_entrez_id;
	  }
	  
	  $subj_entrez_name =~ s/\,/\|/g;
	  $obj_entrez_name =~ s/\,/\|/g;
	  
	  if ($subj_meta_name ne "" && $subj_entrez_name ne "") {
	  	$subj_name_list = $subj_meta_name . "|" . $subj_entrez_name;
	  } elsif($subj_meta_name ne "" && $subj_entrez_name eq "") {
	  	$subj_name_list = $subj_meta_name;
	  } elsif($subj_meta_name eq "" && $subj_entrez_name ne "") {
	  	$subj_name_list = $subj_entrez_name;
	  }
	  
	  if ($obj_meta_name ne "" && $obj_entrez_name ne "") {
	  	$obj_name_list = $obj_meta_name . "|" . $obj_entrez_name;
	  } elsif ($obj_meta_name ne "" && $obj_entrez_name eq "") {
	  	$obj_name_list = $obj_meta_name;
	  } elsif ($obj_meta_name eq "" && $obj_entrez_name ne "") {
	  	$obj_name_list = $obj_entrez_name;
	  }
	  
	  # Retrieving subject novelty value
	  $select_novelty_sth->execute($subj_cui) or die "Couldn't execute statement: " + $select_novelty_sth->errstr;	  	
          $subj_novelty = $select_novelty_sth->fetchrow_array();
          if ($subj_novelty eq 0) {
          	$subj_novelty = 1;
          } else {
          	$subj_novelty = 0;	
	  } 
	  # Retrieving object novelty value
	  $select_novelty_sth->execute($obj_cui) or die "Couldn't execute statement: " + $select_novelty_sth->errstr;	  	
          $obj_novelty = $select_novelty_sth->fetchrow_array();
          if ($obj_novelty eq 0) {
	      	$obj_novelty = 1;
	  } else { 
          	$obj_novelty = 0;
  	  }
          
	  $insert_predication_sth->execute($sentence_id, $pmid, $predicate, $subj_id_list, $subj_name_list,$subj_semtype, $subj_novelty, $obj_id_list, $obj_name_list,$obj_semtype, $obj_novelty);	   $predication_id = $dbh->{'mysql_insertid'};

	  	  
	  # insert the sentence-predication_aux 
	  # print "predication_id = $predication_id\n";
	  
	  # $insert_predication_aux_sth->execute($predication_id,$subj_dist, $subj_maxdist, $subj_start_ind, $subj_end_ind, $subj_text, $subj_score, $indicator_type, $pred_start_ind, $pred_end_ind, $obj_dist, $obj_maxdist, $obj_start_ind, $obj_end_ind, $obj_text, $obj_score) or die "Couldn't execute statement: " + $insert_sentence_predication_sth->errstr;
	  $insert_predication_aux_sth->execute($predication_id,$subj_dist, $subj_maxdist, $subj_start_ind, $subj_end_ind, $subj_text, $subj_score, $indicator_type, $pred_start_ind, $pred_end_ind, $obj_dist, $obj_maxdist, $obj_start_ind, $obj_end_ind, $obj_text, $obj_score);
	$prev_sentence_id = $sentence_id;
	$prev_pmid = $pmid;
	$prev_type = $senttype;
	$prev_number = $number;
      }
    }
}
close (F);
  
  print "Semrepping is done!\n\n";



# an argument can be from UMLS, from EntrezGene or clinical trials
# intervention list.
# Some semtypes may not be natural. (For instance, "aapp" semtype
# gets "gngm" automatically.)
# Read the argument information needed from all this weirdness
sub ReadArgumentUIs {
  my ($meta_name, $cui, $semtype, $entrez_id, $entrez_name) = @_;
  @uis = ();
  # do we really need to check for gngm and aapp?
  if ($semtype eq "gngm" || $semtype eq "aapp") {
    if ($entrez_id eq "") {
      if ($entrez_name eq "None" && $meta_name eq "") {
	push @uis, "0";
      }
    } else {
      # entrezgene ids may be multiple.
      @entrezgene_ids = split(/,/, $entrez_id);
      push @uis, @entrezgene_ids;
    }
  }
  unless ($cui eq "" || $cui eq "C0000000") {
    @cuis = split(/,/, $cui);
    push @uis, @cuis;
  }
  else {
    unless ($meta_name eq "") {
      if (exists $seen_interventions{$meta_name}) {
	$cui = $seen_interventions{$meta_name};
	push @uis, $cui;
      } else {
	# special case, this will rarely happen
	# if the concept is from ctrials intervention list, no UMLS concept can match.
	# we have to use meta_name in this case.
	$select_concept_ct_sth->execute($meta_name) or die "Couldn't execute statement: " + $select_concept_ct_sth->errstr;
	# print "Searching for intervention: $meta_name\n";
	while ($cui = $select_concept_ct_sth->fetchrow_array()) {
	  if ($cui =~ /^I/) {
	    push @uis, $cui;
	    $seen_interventions{$meta_name} = $cui;
	    # print "Found intervention: $meta_name | $cui\n";
	    last;
	  }
	}
      }
    }
  }
  return @uis;
}

# Get the concept semtype od for the given argument.
sub GetConceptSemtypeIDs {
  my($uilist, $semtype_list, $semtype) = @_;
  @uis = @{$uilist};
  @semtypes = @{$semtype_list};
  my $first_novelty = "";
  my $novelty_retrieved = 0;
  # put the concept_semtype_ids in hashes for later use
  foreach $ui (@uis) {
    unless (exists $seen_concs{$ui}{$semtype}) {
      $select_concept_sth->execute($ui, $semtype) or die "Couldn't execute statement: " + $select_concept_sth->errstr;
      $concsem_id = $select_concept_sth->fetchrow_array(); 
      # concept_semtype is not in db
      # probably, a synthetic semtype is being used
      if ($concsem_id eq "") {
	if ($semtype eq "gngm" || $semtype eq "aapp") {
	  foreach $sem (@semtypes) {
	    unless ($sem eq $semtype) {
	      $select_concept_sth->execute($ui,$sem) or die "Couldn't execute statement: " + $select_concept_sth->errstr;
	      # $concsem_id = $select_concept_sth->fetchrow_array();
	      $concsem_id = $select_concept_sth->fetchrow_array();
	      unless ($concsem_id eq "") { last;}
	    }
	  }
	} elsif ($semtype eq "humn") {
	  $select_humn_sth->execute($ui) or die "Couldn't execute statement: " + $select_human_sth->errstr;
	  $concsem_id = $select_humn_sth->fetchrow_array();
	}
      }
      unless ($concsem_id eq "") {
	$seen_concs{$ui}{$semtype} = $concsem_id;
      }   	
    }      		
  }

}

# having the subject and object ids and the predicate,
# find the predication id in the db
# this is complicated by the fact that an argument
# may have more than one concept.
# so the idea is to find the predication ids associated
# with each and intersect them.
# Only one predication_id must match at the end.
# The result is added to a hash for later retrieval.
sub GetPredID {
  my ($subj_ui_list, $obj_ui_list, $predicate) = @_;
  my (@subj_uis) = @{$subj_ui_list};
  my (@obj_uis) = @{$obj_ui_list};

  $i=0;
  $argcount = scalar(@subj_uis) + scalar(@obj_uis);
  $predid_found = 0;
  undef $concept_id;

  $concept_semtype_id = $seen_concs{$subj_uis[0]}{$subj_semtype};
  # print "Concept SemType Id: $concept_semtype_id \n";
  if ($concept_semtype_id eq "") { return -1;}
  $query_str = "SELECT P.PREDICATION_ID FROM PREDICATION P, PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE P.PREDICATE=\'" . $predicate . "\' AND P.TYPE=\'" . $type . "\' AND CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='S' AND P.PREDICATION_ID=PA.PREDICATION_ID AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID";

  for ($i=1; $i< (scalar @subj_uis); $i++) {
    $concept_semtype_id = $seen_concs{$subj_uis[$i]}{$subj_semtype};
  # print "Concept SemType Id: $concept_semtype_id\n";
    if ($concept_semtype_id eq "") { return -1;}
    $query_str .=  " AND PA.PREDICATION_ID IN (SELECT DISTINCT PA.PREDICATION_ID FROM PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='S' AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID) ";
  }
  for ($i=0; $i< (scalar @obj_uis); $i++) {
    $concept_semtype_id = $seen_concs{$obj_uis[$i]}{$obj_semtype};
    if ($concept_semtype_id eq "") { return -1;}
    # print "Concept SemType Id: $concept_semtype_id\n";
    $query_str .= " AND PA.PREDICATION_ID IN (SELECT DISTINCT PA.PREDICATION_ID FROM PREDICATION_ARGUMENT PA, CONCEPT_SEMTYPE CS WHERE CS.CONCEPT_SEMTYPE_ID=\'" . $concept_semtype_id . "\' AND PA.TYPE='O' AND PA.CONCEPT_SEMTYPE_ID=CS.CONCEPT_SEMTYPE_ID)";
  }
#  $query_str .= " GROUP BY PA.PREDICATION_ID";
  # print "Predication Query: $query_str\n";
  $select_predid_sth = $dbh->prepare($query_str) or die "Couldn't prepare statement: " . $dbh->errstr;
  $select_predid_sth->execute() or die "Couldn't execute statement: " . $dbh->errstr;
#  $pred_id = $select_predid_sth->fetchrow_array();
  while ($pred_id = $select_predid_sth->fetchrow_array()) {
    $select_pa_count_sth->execute($pred_id) or die "Couldn't execute statement: " . $select_pa_count_sth->errstr;
    $predid_cnt = $select_pa_count_sth->fetchrow_array();
    # print "Argument counts: $pred_id | $predid_cnt | $argcount\n";
    if ($predid_cnt  == $argcount) {
      $predid_found = 1;
      last;
    }
  }
  if ($predid_found == 1) {
    $key = join("|", sort @subj_uis) . "|" . $subj_semtype . "|" . $predicate . "|". join("|", sort @obj_uis) . "|" . $obj_semtype;
    $seen_predids{$key} = $pred_id;
    # print "Predication Id: $pred_id\n";
    return $pred_id;
  }
  

  return "";

}
