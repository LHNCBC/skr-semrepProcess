README for SemRep Processing

Dongwook Shin, Ph.D.
Date: August 15 2019
Java package: gov.nih.nlm.skr.semrepProcess
Data format:
Input: XML format compliant with MEDLINE BASELINE converts XML files into the corresponding ASCII format (a separate directory) and generating input ASCII file for semrep processing. The input file format for Semrep is:
First line: PMID- pmid_number
Second line: TI  - title
Third line: AB  - abstract

EXAMPLE:
PMID- 30
TI  - Lysosomal hydrolases of the epidermis. I. Glycosidases.
AB  - Seven distinct glycosidases (EC 3.2) have been characterized in guinea-pig epidermis. Their properties indicate them to be of lysosomal origin. The 'profile' of 
the epidermal glycosidases is significantly different from that reported for whole skin, the activities of beta-galactosidase and beta-acetylglucosaminidase being very
 high and those of the remaining enzymes relatively low in epidermis.
	
Process
1.	Create two databases; one for PREPROCESS and the other for SemMedDatabase
a.	The database schema for each database is:
CREATE TABLE FACT_DATA (
        `FACT_DATA_ID` int(10) unsigned NOT NULL auto_increment,
        PMID varchar(20) NOT NULL,
        EXIST_XML tinyint(1) default 0,
        EXIST_SEMREP tinyint(1)  default 0,
        XML_DATA MEDIUMTEXT,
        SEMREP_DATA MEDIUMTEXT,
        TITLE varchar(999), 
        ABSTRACT MEDIUMTEXT,
        PRIMARY KEY (`FACT_DATA_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ;
create index pmid_index_btree using btree on FACT_DATA (PMID);

b.	The database schema for SemMedDB is described in appendix A.
This database name can be configured in ROOT/semrep.properties file as follows:
connectionString=jdbc:mysql://indsrv2.nlm.nih.gov
preddatabase=PREPROCESS
semmeddatabase=semmedVER50
dbusername=user name for the database
dbpassword=password for the database
Connection string is the connection string for MySQL database that Is to the Semrep output. 
Please note that the user has to have the read/write privilege for the database.

2.	Check if there is a jar file ./lib/semrepProcess.jar. If not, run the following command and generate it. 
a.	% ant main -f build_semrepProcess.xml
If a Java program in ./src directory is modified for some reason, make sure to run the 'ant' command and have the latest semrepProcess.jar file.
3.	Set the parameters appropriately in the file ROOT/semrep.properties. The parameters relevant to Semrep processing in addition to those described in 1.b are as follows:
semrepLoadingProgram=resources/semrepping_40WithSH.pl
semrepentityLoadingProgram=resources/semrepping_entity_40.pl
perlScript=/usr/bin/perl
schedulerAccount=yourUMLSAccount
schedulerPassword=yourUMLSPassword

semrepLoadingProgram is the Perl script that is used in loading SemRep output into the database, whereas semrepentityLoadingProgram is the Perl script that loads Entity into ENTITY table. As ENTITY table is optional in SemMed database, it is not loaded while predications and sentences are loaded. Instead, it is loaded separately.
schedulerAccout and schedulerPassword are the account information that is used to login to UMLS.
4.	Run gov.nih.nlm.skr.semrepProcess.Semrepping. This class takes (1) the MEDLINE_BASELINE directory as input, (2) converting XML files into corresponding ASCII format, (3) generating input ASCII file for Semrep processing and (4) putting Semrep result. The shell script ‘semrepping.sh' does this step automatically. The arguments need to be:
a.	First argument - MEDLINE_BASELINE directory
b.	Second argument - ASCII conversion directory from MEDLINE_BASELINE
c.	Third argument - directory for Medline format 
d.	Forth argument - directory for Semrep output
-	In this process, PMID, title and abstract are inserted into the FACT_DATA table for the corresponding row. This is because if Semrep step fails for some citations in Step 3 (4), the Normalized format and MEDLINE format are recreated from these tables and resubmitted in step 5.
-	The reason that this class needs ASCII converted XML directory is that it extracts metadata information from XML structure and inserts it into CITATIONS table in the SemMed database before any sentence and predication is inserted.
-	From SemMedDB version 4.0, SENTENCE table is dependent on CITATIONS table and unless the metadata info for a PMID is created in CITATIONS table, the sentences are not inserted into SENTENCE table.
-	Semrep results will be loaded into SemMed database using the perl semrep loading program. It is configured in ROOT/factuality_semrep.properties file as:
semrepLoadingProgram=resources/semrepping_40WithSH.pl
perlScript=/usr/bin/perl
-	This step can be performed using ROOT/semrepping.sh

5.	gov.nih.nlm.skr.semrepProcess.semrepingFromDB. This class is looking at the PREPROCESS database and finds the PMIDs whose EXIST_SEMREP is 0 instead of 1. If EXIST_SEMREP is 0, it means that the citation has not been semrepped yet (either because it was not semrepped at all or semrepping has failed) and needs to be semrepped in this step. SemreppingFromDB class is scanning from FACT_DATA table, extracting limit PMIDs starting from offset, generating input MEDLINE format, and sending those to the Scheduler. The output SemRep result is stored in the temp directory specified in the third argument (see below). The shell script 'SemreppingFromDB.sh' does this task automatically. This class takes three input parameters as follows:
a.	First argument is the integer specifying the limit, which is a number of each batch (number of MEDLINE citations that are to be taken to the Scheduler)
b.	Second argument is the starting offset from which the PMIDs whose EXIST_SEMREP is 0 are extracted.
c.	Third argument is the temp directories where SemRep output is stored and loaded into the SemMedDB database.
d.	As SemRepping of some citations can fail in the Scheduler for various reasons (including timeout or Scheduler failure), gov.nih.nlm.skr.semrepProcess.SemrepingFromDB class needs to be executed repeatedly until the remaining citations cannot be semrepped any further.

6.	Sometimes, in Step 5, Semrep results fail to be loaded into the database. In that case, use the class gov.nih.nlm.skr.semrepProcess.LoadingSemrepToDB and load the SemRep outputs into the SemMed database. There is a shell script 'loadsemrep2semmeddb.sh' that does this process automatically. The input arguments are:
a.	First argument is the directory where SemRep output is located
b.	The extension of SemRep output file. In the sample, the extension is 'semrep'.
c.	The third argument is the starting number of the SemRep file in the directory specified in a.
d.	The fourth argument is the ending number of the SemRep file in the directory.

7.	After all the SemRep results are loaded into the database, make sure that the predications and sentences are in PREDICATION and SENTENCE table, respectively. At this stage, NORMALIZED_SECTION_HEADER is still null, while SECTION_HEADER is populated from the SemRep output. So the class gov.nih.nlm.skr.PopulateNormalizedSH converts SECTION_HEADER into normalized section header and populates that into NORMALIZED_SECTION_HEADER column. In this process, the file SectionHeaderNormalization.txt is used, where the first column indicates possible section header and the second column shows the matching normalized section header. A shell script populateNormalizedSH.sh does this step automatically. The input arguments are:
a.	The first argument is the file name that has the mapping from Section Header to Normalized Section header
b.	The second argument is the start location of row in SENTENCE table. Normally it is 0. But when you want to start this operation at the n-th row of SENTENCE table, you need to give the number 'n'.
c.	The third argument is the limit of the select statement. As the number of predications is too big, these operation can be done in batch matter, selecting 'limit' numbers of SENTENCE each time in order to reduce the memory consumption.

8.	In PREDICATION table, the novelty column is still not populated and you need to populate it to SUBJ_NOVELTY and OBJECT_NOVELTY column. gov.nih.nlm.skr.PopulateNoveltyToPredication does this procedure. A shell script populateNovelty.sh does this step automatically.
a.	The first argument is the start location of row in PREDICATION table. Normally it is 0. But when you want to start this operation at the n-th row of PREDICATION table, you need to give the number 'n'.
b.	The second argument is the limit of the select statement. As the number of predications is too big, these operation can be done in batch matter, selecting 'limit' numbers of SENTENCE each time in order to reduce the memory consumption.

9.	At this point, SemRep results are loaded into tables, but entity information is not. The reason that entity information is not loaded when predications are loaded is that entity information is optional and the table is huge. So if you want to load entity information, use the class gov.nih.nlm.skr.LoadSemrepEntity, whose arguments are as follows:
LoadSemrepEntity class extracts the SemRep result saved in SEMREP_DATA column in FACT_DATA of PREPROCESS database and loads them into the ENTITY table in the SemMed database. In doing so, it starts from the offset, which is the second argument and extracts limit number of PMIDs at one time. LoadSemrepEntity class uses the following SQL statement and extracts SemRep output:
Select * from FACT_DATA where EXIST_SEMREP = 1 limit = limit offset = offset

Appendix A. Database schema for SemMed database
-- table CITATIONS
CREATE TABLE METAINFO (
        DBVERSION varchar(10) NOT NULL,
        SEMREPVERSION varchar(10),
        PUBMED_TODATE varchar(10),
        COMMENT varchar(500)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE CITATIONS (
        PMID varchar(20) NOT NULL,
        ISSN varchar(10),
        DP varchar(50),
        EDAT varchar(50),
        PYEAR int(5),
        PRIMARY KEY (`PMID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ;

CREATE TABLE `GENERIC_CONCEPT` (
  `CONCEPT_ID` int(10) unsigned NOT NULL auto_increment,
  `CUI` varchar(20) NOT NULL default '',
  `PREFERRED_NAME` varchar(200) character set utf8 NOT NULL default '',
  PRIMARY KEY (`CONCEPT_ID`),
  KEY `PREFERRED_NAME` (`PREFERRED_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores basic UMLS concept information';


-- table SENTENCE
CREATE TABLE `SENTENCE` (
  `SENTENCE_ID` int(10) unsigned NOT NULL auto_increment,
  `PMID` varchar(20) NOT NULL default '',
  `TYPE` varchar(2) NOT NULL default '',
  `NUMBER` int(10) unsigned NOT NULL default '0',
  `SENT_START_INDEX` int(10) unsigned NOT NULL default '0',
  `SENT_END_INDEX` int(10) unsigned NOT NULL default '0',
  `SECTION_HEADER` varchar(100),
  `NORMALIZED_SECTION_HEADER` varchar(50),
  `SENTENCE` varchar(999) character set utf8 NOT NULL default '',
  PRIMARY KEY (`SENTENCE_ID`),
  FOREIGN KEY (`PMID`) REFERENCES `CITATIONS` (`PMID`) ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY `SENTENCE` (`PMID`,`TYPE`,`NUMBER`),
  KEY `PMID_INDEX` USING BTREE (`PMID`),
  KEY `PMID_HASH` USING HASH (`PMID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores sentences from Medline';

CREATE TABLE `PREDICATION` (
        PREDICATION_ID int(10) unsigned  NOT NULL auto_increment,
        SENTENCE_ID int(10) unsigned  NOT NULL ,
        PMID varchar(20),
        PREDICATE varchar(50),
        SUBJECT_CUI varchar(255),
        SUBJECT_NAME varchar(999),
        SUBJECT_SEMTYPE varchar(50),
        SUBJECT_NOVELTY tinyint(1),
        OBJECT_CUI varchar(255),
        OBJECT_NAME varchar(999),
        OBJECT_SEMTYPE varchar(50),
        OBJECT_NOVELTY tinyint(1),
        FACT_VALUE char(20),
	MOD_SCALE char(20),
        MOD_VALUE float(3,2),
        PRIMARY KEY (PREDICATION_ID),
-- Commented on March 23 2010, Dongwook since InnoDB cannot be created because of Foreign key constraint
-- In "Creating InnoDB Tables" and the title "FOREIGN KEY Constraints"... 
-- Foreign keys definitions are subject to the following conditions: 
-- Both tables must be InnoDB type. 
-- In the referencing table, there must be an index where the foreign key columns are listed as the first columns in the same order. 
-- In the referenced table, there must be an index where the referenced columns are listed as the first columns in the same order. 
-- Index prefixes on foreign key columns are not supported. One consequence of this is that BLOB and TEXT columns cannot be included in a foreign key, because indexes on those columns must always include a prefix length.	
  FOREIGN KEY (SENTENCE_ID) REFERENCES SENTENCE(SENTENCE_ID) ON DELETE CASCADE  ON UPDATE CASCADE,
  FOREIGN KEY (PMID) REFERENCES SENTENCE(PMID) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores aggregate info of semantic predications';
create index pmid_index_btree using btree on PREDICATION (PMID);

-- table PREDICATION_AUX
CREATE TABLE `PREDICATION_AUX` (
  `PREDICATION_AUX_ID` int(10) unsigned NOT NULL auto_increment,
  `PREDICATION_ID` int(10) unsigned NOT NULL,
  `SUBJECT_TEXT` varchar(200) default '' COMMENT 'Should be NOT NULL eventually',
  `SUBJECT_DIST` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `SUBJECT_MAXDIST` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `SUBJECT_START_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `SUBJECT_END_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `SUBJECT_SCORE` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `INDICATOR_TYPE` varchar(10) default '' COMMENT 'Should be NOT NULL eventually',
  `PREDICATE_START_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `PREDICATE_END_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_TEXT` varchar(200) default '' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_DIST` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_MAXDIST` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_START_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_END_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `OBJECT_SCORE` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `CURR_TIMESTAMP` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`PREDICATION_AUX_ID`),
  FOREIGN KEY (`PREDICATION_ID`) REFERENCES `PREDICATION` (`PREDICATION_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores semantic predications in sentences';


-- table COREFERENCE
CREATE TABLE `COREFERENCE` (
  `COREFERENCE_ID` int(10) unsigned NOT NULL auto_increment,
  `PMID` varchar(20) NOT NULL default '',
  `ANA_CUI` varchar(255),
  `ANA_NAME` varchar(999),      
  `ANA_SEMTYPE` varchar(50),
  `ANA_TEXT` varchar(200) default '',
  `ANA_SENTENCE_ID` int(10) unsigned NOT NULL,
  `ANA_START_INDEX` int(10) unsigned default '0' ,
  `ANA_END_INDEX` int(10) unsigned default '0' ,
  `ANA_SCORE` int(10) unsigned default '0' ,
  `ANT_CUI` varchar(255),
  `ANT_NAME` varchar(999),      
  `ANT_SEMTYPE` varchar(50),
  `ANT_TEXT` varchar(200) default '',
  `ANT_SENTENCE_ID` int(10) unsigned NOT NULL,
  `ANT_START_INDEX` int(10) unsigned default '0' ,
  `ANT_END_INDEX` int(10) unsigned default '0' ,
  `ANT_SCORE` int(10) unsigned default '0' ,
  `CURR_TIMESTAMP` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY  (`COREFERENCE_ID`),
  UNIQUE KEY (`PMID`,`ANA_START_INDEX`,`ANA_END_INDEX`, `ANT_START_INDEX`,`ANT_END_INDEX`),
  CONSTRAINT `PMID_ibfk_1` FOREIGN KEY (`PMID`) REFERENCES `SENTENCE` (`PMID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ANA_SENTENCE_ID_ibfk_4` FOREIGN KEY (`ANA_SENTENCE_ID`) REFERENCES `SENTENCE` (`SENTENCE_ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ANT_SENTENCE_ID_ibfk_5` FOREIGN KEY (`ANT_SENTENCE_ID`) REFERENCES `SENTENCE` (`SENTENCE_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores coreference';


-- table ENTITY
CREATE TABLE `ENTITY` (
  `ENTITY_ID` int(10) unsigned NOT NULL auto_increment,
  `SENTENCE_ID` int(10) unsigned NOT NULL,
  `CUI` varchar(255),
  `NAME` varchar(999),
  `SEMTYPE` varchar(50),
  `GENE_ID` varchar(20) NOT NULL default '',
  `GENE_NAME` varchar(20) NOT NULL default '',
  `TEXT` varchar(200) default '',
  `SCORE` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `START_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  `END_INDEX` int(10) unsigned default '0' COMMENT 'Should be NOT NULL eventually',
  PRIMARY KEY  (`ENTITY_ID`),
  FOREIGN KEY (`SENTENCE_ID`) REFERENCES `SENTENCE` (`SENTENCE_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Stores semantic predications in sentences';
