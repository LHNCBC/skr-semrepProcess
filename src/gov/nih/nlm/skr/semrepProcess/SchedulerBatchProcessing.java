package gov.nih.nlm.skr.semrepProcess;
/*
===========================================================================
*
*                            PUBLIC DOMAIN NOTICE
*               National Center for Biotechnology Information
*         Lister Hill National Center for Biomedical Communications
*
*  This software is a "United States Government Work" under the terms of the
*  United States Copyright Act.  It was written as part of the authors' official
*  duties as a United States Government contractor and thus cannot be
*  copyrighted.  This software is freely available to the public for use. The
*  National Library of Medicine and the U.S. Government have not placed any
*  restriction on its use or reproduction.
*
*  Although all reasonable efforts have been taken to ensure the accuracy
*  and reliability of the software and data, the NLM and the U.S.
*  Government do not and cannot warrant the performance or results that
*  may be obtained by using this software or data. The NLM and the U.S.
*  Government disclaim all warranties, express or implied, including
*  warranties of performance, merchantability or fitness for any particular
*  purpose.
*
*  Please cite the authors in any work or product based on this material.
*
===========================================================================
*/

/**
 * Example program for submitting a new SemRep Batch job request to the
 * Scheduler to run.  You will be prompted for your username and password
 * and if they are alright, the job is submitted to the Scheduler and the
 * results are returned in the String "results" below.
 *
 * This example shows how to setup a basic SemRep Batch job with a small
 * file (sample.txt) with ASCII MEDLINE formatted citations as input data.
 * You must set the Email_Address variable and use the UpLoad_File to specify
 * the data to be processed.  This example also shows the user setting the
 * silentEmail option which tells the Scheduler to NOT send email upon
 * completing the job.
 *
 * This example also shows one way to setup which arguments you want to use
 * for the SemRep program ("mySemRepObj.setArgs("-D");". This tells SemRep
 * to use the "Full Fielded Output" when processing the data. The results that
 * come back from SemRep are then just printed to the standard output.
 *
 * @author	Jim Mork
 * @version	1.0, September 18, 2006
**/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import gov.nih.nlm.nls.skr.GenericObject;

public class SchedulerBatchProcessing {

    private static SchedulerBatchProcessing myInstance;

    public static SchedulerBatchProcessing getInstance() {
	if (myInstance == null)
	    synchronized (SchedulerBatchProcessing.class) {
		if (myInstance == null)
		    myInstance = new SchedulerBatchProcessing();
	    }
	return myInstance;
    }
    // With SKR Version  2.0, July 2011
    /*
     * private static GenericObject myGenericObj; public static GenericObject
     * getInstance(){ if (myGenericObj==null) synchronized(GenericObject.class){
     * if(myGenericObj==null) myGenericObj = new GenericObject(); } return
     * myGenericObj; }
     */

    public void submitTask(String inputfile, String outputfile, String semrepCom, String username, String password,
	    String email) throws RuntimeException, IOException {

	// SemRepObject mySemRepObj = new SemRepObject(username, password);
	//  mySemRepObj.setField("Email_Address", email); 
	// mySemRepObj.setField("UpLoad_File", inputfile);

	// NOTE: You MUST specify an email address because it is used for
	//       logging purposes.
	System.out.println("Username : " + username);
	System.out.println("Password : " + password);
	System.out.println("email : " + email);
	System.out.println("Upload file : " + inputfile);
	GenericObject myGenericObj = new GenericObject(username, password);
	// myGenericObj.setField("Username", username);
	// myGenericObj.setField("Password", password);
	myGenericObj.setField("Email_Address", email);
	myGenericObj.setFileField("UpLoad_File", inputfile);
	myGenericObj.setField("Batch_Command", semrepCom);
	myGenericObj.setField("BatchNotes", "Semrep");
	// myGenericObj.setField("SilentEmail", true);

	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
	String results = myGenericObj.handleSubmission();
	out.write(results);
	out.close();
    } // main

} // class SemRepBatch
