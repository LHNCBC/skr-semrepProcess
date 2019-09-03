package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseBatch extends Thread {
    String perlPath = null;
    String perlProgramPath = null;
    String semrepFilePath = null;
    String dbName = null;
    String dbAccount = null;
    String dbPassword = null;
    String normFile = null;

    public void setPath(String perlPathArg, String perlProgramArg, String semrepFilePathArg, String dbNameArg,
	    String dbAccountArg, String dbPasswordArg, String normFileArg) {
	perlPath = perlPathArg;
	perlProgramPath = perlProgramArg;
	semrepFilePath = semrepFilePathArg;
	dbName = dbNameArg;
	dbAccount = dbAccountArg;
	dbPassword = dbPasswordArg;
	normFile = normFileArg;
    }

    @Override
    public void run() {
	try {
	    String cmd[] = { perlPath, perlProgramPath, dbName, semrepFilePath, dbAccount, dbPassword, normFile };
	    System.out.println("perl path = " + perlPath);
	    System.out.println("perlProgramPath = " + perlProgramPath);
	    System.out.println("dbname = " + dbName);
	    System.out.println("semrepFilePath = " + semrepFilePath);
	    System.out.println("Normization File Name = " + normFile);
	    Runtime r = Runtime.getRuntime();
	    // String cmd[]={"/usr/bin/perl", perl1Realpath, "semmed2006", tempSemrepRealpath};
	    Process p = r.exec(cmd);

	    InputStream inputstream = p.getInputStream();
	    InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
	    BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
	    // read the ls output

	    String line;
	    while ((line = bufferedreader.readLine()) != null) {
		System.out.println(line);
	    }
	    // check for ls failure
	    if (p.waitFor() != 0) {
		System.out.println("exit value = " + p.exitValue());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
