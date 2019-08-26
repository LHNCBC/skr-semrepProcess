package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PopulateNoveltyToPredicationFile {

    static public void main(String args[]) {
	try {
	    Class.forName("com.mysql.jdbc.Driver");

	    Connection conn1 = DriverManager.getConnection(
		    "jdbc:mysql://indsrv2.nlm.nih.gov/" + args[0] + "?autoReconnect=true", "root", "indsrv2@root");

	    Statement stmt1 = conn1.createStatement();
	    Statement stmt2 = conn1.createStatement();
	    // String SQLselect = new String("select DISTINCT(c.CUI),
	    // c.PREFERRED_NAME from CONCEPT as c, CONCEPT_SEMTYPE as cs where
	    // c.CONCEPT_ID = cs.CONCEPT_ID and cs.NOVEL = 'N'");
	    String SQLselect = new String(
		    "select PREDICATION_ID, SUBJECT_CUI, OBJECT_CUI from PREDICATION  where PMID = \"111\"");
	    String SQLselect2Tmp = new String("select count(*) from GENERIC_CONCEPT where CUI = '");
	    String SQLupdateOrg = new String(
		    "update PREDICATION set SUBJECT_NOVELTY = 1, OBJECT_NOVELTY = 2 where PREDICATION_ID = ");
	    BufferedReader br = new BufferedReader(new FileReader(args[1]));
	    int total = 0;
	    String line = null;

	    int count = 0;
	    while ((line = br.readLine()) != null) {
		String SQLselectTmp = SQLselect.replace("111", line.trim());
		// System.out.println(SQLselectTmp);
		ResultSet rs1 = stmt1.executeQuery(SQLselectTmp);
		while (rs1.next()) {
		    int predicationid = rs1.getInt(1);
		    String scui = rs1.getString(2);
		    String ocui = rs1.getString(3);

		    ResultSet rs2 = stmt2.executeQuery(new String(SQLselect2Tmp + scui + "'"));
		    rs2.first();
		    int count1 = rs2.getInt(1);

		    ResultSet rs3 = stmt2.executeQuery(new String(SQLselect2Tmp + ocui + "'"));
		    rs3.first();
		    String SQLupdate = null;
		    int count2 = rs3.getInt(1);
		    if (count1 > 0 && count2 > 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "0").replaceFirst("2", "0");
		    } else if (count1 == 0 && count2 > 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "1").replaceFirst("2", "0");
		    } else if (count1 > 0 && count2 == 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "0").replaceFirst("2", "1");
		    } else if (count1 == 0 && count2 == 0) {
			SQLupdate = SQLupdateOrg.replaceFirst("1", "1").replaceFirst("2", "1");
		    }
		    SQLupdate = new String(SQLupdate + predicationid);
		    // System.out.println(SQLupdate);
		    stmt2.executeUpdate(SQLupdate);

		}
		count++;
		System.out.println(count);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
