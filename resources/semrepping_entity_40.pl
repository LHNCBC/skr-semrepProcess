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
$insert_entity_sth = $dbh->prepare_cached('INSERT INTO ENTITY (SENTENCE_ID, CUI, NAME, SEMTYPE, GENE_ID, GENE_NAME, TEXT, SCORE, START_INDEX, END_INDEX) VALUES(?,?,?,?,?,?,?,?,?,?)'); 

$select_sentence_sth = $dbh->prepare_cached('SELECT SENTENCE_ID FROM SENTENCE WHERE PMID=? AND TYPE=? AND NUMBER=?') or die "Couldn't prepare statement: " . $dbh->errstr;
$delete_sentence_sth = $dbh->prepare_cached('DELETE P FROM PREDICATION P, PREDICATION_AUX PA, SENTENCE S WHERE S.PMID=?  AND P.PREDICATION_ID=PA.PREDICATION_ID AND S.SENTENCE_ID=P.SENTENCE_ID') or die "Couldn't prepare statement: " . $dbh->errstr;

$delete_predication_sth = $dbh->prepare_cached('DELETE P FROM PREDICATION P WHERE P.SENTENCE_ID=?') or die "Couldn't prepare statement: " . $dbh->errstr;

$delete_entity_sth = $dbh->prepare_cached('DELETE E FROM ENTITY E WHERE E.SENTENCE_ID=?') or die "Couldn't prepare statement: " . $dbh->errstr;

# $select_pmid_sentence_sth = $dbh->prepare_cached('SELECT COUNT(*) FROM SENTENCE WHERE PMID = ?') or die "Couldn't prepare statement: " . $dbh->errstr;

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
      # }      	# get the sentence_id in the db for the given sentence.
      	
	$select_sentence_sth->execute($pmid,$senttype,$number) or die "Couldn't execute statement: " + $select_sentence_sth->errstr;
	
        $sentence_id = $select_sentence_sth->fetchrow_array();


      # we are looking at the text line. Insert or update the sentence record.
     
      # Version 3.0 handles the entities as well as predication      
      if ($line_elements[5] eq "entity") {
      
      	$cui = $line_elements[6];
      
      	$meta_name = $line_elements[7];
      	
      	$semtype = $line_elements[8];
      
      	$entrez_id = $line_elements[9];
      
      	$entrez_name = $line_elements[10];
      
      	$text = $line_elements[11];
      
      	$score = $line_elements[15];
      
      	$start_index = $line_elements[16];
      
	$end_index = $line_elements[17];
	
	if($sentence_id) {
	
		$insert_entity_sth->execute($sentence_id, $cui, $meta_name, $semtype, $entrez_id, $entrez_name, $text, $score, $start_index, $end_index) or die "Couldn't execute statement: " . $sth->errstr;
		
	}
      
      }
    }

 }   
close (F);
  
  print "Inserting entity is done!\n\n";
