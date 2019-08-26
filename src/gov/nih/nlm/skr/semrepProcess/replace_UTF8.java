package gov.nih.nlm.skr.semrepProcess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/* replace_UTF8 is a very simple program used to convert non-ASCII characters
   to ASCII where we have a mapping.  If there is no mapping in the HashMap
   below, the non-ASCII character will be converted to a question mark '?'.
   Most mappings are one to one, others like Greek alphabet characters
   are spelled out.

   Usage: 

     cat file | java replace_UTF8 > result

    or

     java replace_UTF8 file > result 
*/

/*
    To determine the '\u0023' value of a non-ASCII char in Emacs,
    mouse over the character, and do
    type Control-X =
    Then use the value of "#x223c" in the char_map.put call:

    char_map.put('\u223c', "~");
*/

import java.util.HashMap;

public class replace_UTF8 {
    static HashMap<Character, String> char_map = new HashMap<>();
    static {
	char_map.put('\u0020', " ");
	char_map.put('\u0009', " "); // horizontal tab
	char_map.put('\u0021', "!");
	char_map.put('\u0022', "\"");
	char_map.put('\u0023', "#");
	char_map.put('\u0024', "$");
	char_map.put('\u0025', "%");
	char_map.put('\u0026', "&");
	// https://stackoverflow.com/questions/13693312/java-assign-unicode-apostrophe-to-char
	char_map.put('\u005c\u0027', "'");
	char_map.put('\u0028', "(");
	char_map.put('\u0029', ")");
	char_map.put('\u002a', "*");
	char_map.put('\u002b', "+");
	char_map.put('\u002c', ",");
	char_map.put('\u002d', "-");
	char_map.put('\u002e', ".");
	char_map.put('\u002f', "/");
	char_map.put('\u0030', "0");
	char_map.put('\u0031', "1");
	char_map.put('\u0032', "2");
	char_map.put('\u0033', "3");
	char_map.put('\u0034', "4");
	char_map.put('\u0035', "5");
	char_map.put('\u0036', "6");
	char_map.put('\u0037', "7");
	char_map.put('\u0038', "8");
	char_map.put('\u0039', "9");
	char_map.put('\u003a', ":");
	char_map.put('\u003b', ";");
	char_map.put('\u003c', "<");
	char_map.put('\u003d', "=");
	char_map.put('\u003e', ">");
	char_map.put('\u003f', "?");
	char_map.put('\u0040', "@");
	char_map.put('\u0041', "A");
	char_map.put('\u0042', "B");
	char_map.put('\u0043', "C");
	char_map.put('\u0044', "D");
	char_map.put('\u0045', "E");
	char_map.put('\u0046', "F");
	char_map.put('\u0047', "G");
	char_map.put('\u0048', "H");
	char_map.put('\u0049', "I");
	char_map.put('\u004a', "J");
	char_map.put('\u004b', "K");
	char_map.put('\u004c', "L");
	char_map.put('\u004d', "M");
	char_map.put('\u004e', "N");
	char_map.put('\u004f', "O");
	char_map.put('\u0050', "P");
	char_map.put('\u0051', "Q");
	char_map.put('\u0052', "R");
	char_map.put('\u0053', "S");
	char_map.put('\u0054', "T");
	char_map.put('\u0055', "U");
	char_map.put('\u0056', "V");
	char_map.put('\u0057', "W");
	char_map.put('\u0058', "X");
	char_map.put('\u0059', "Y");
	char_map.put('\u005a', "Z");
	char_map.put('\u005b', "[");
	char_map.put('\u005d', "]");
	char_map.put('\u005e', "^");
	char_map.put('\u005f', "_");
	char_map.put('\u0060', "'");
	char_map.put('\u0061', "a");
	char_map.put('\u0062', "b");
	char_map.put('\u0063', "c");
	char_map.put('\u0064', "d");
	char_map.put('\u0065', "e");
	char_map.put('\u0066', "f");
	char_map.put('\u0067', "g");
	char_map.put('\u0068', "h");
	char_map.put('\u0069', "i");
	char_map.put('\u006a', "j");
	char_map.put('\u006b', "k");
	char_map.put('\u006c', "l");
	char_map.put('\u006d', "m");
	char_map.put('\u006e', "n");
	char_map.put('\u006f', "o");
	char_map.put('\u0070', "p");
	char_map.put('\u0071', "q");
	char_map.put('\u0072', "r");
	char_map.put('\u0073', "s");
	char_map.put('\u0074', "t");
	char_map.put('\u0075', "u");
	char_map.put('\u0076', "v");
	char_map.put('\u0077', "w");
	char_map.put('\u0078', "x");
	char_map.put('\u0079', "y");
	char_map.put('\u007a', "z");
	char_map.put('\u007b', "{");
	char_map.put('\u007c', "|");
	char_map.put('\u007d', "}");
	char_map.put('\u007e', "~");
	char_map.put('\u00a0', " ");
	char_map.put('\u00a1', "!");
	char_map.put('\u00a2', "c"); // Cent sign
	char_map.put('\u00a3', "L"); // Pound sign
	char_map.put('\u00a4', " "); // Currency sign
	char_map.put('\u00a5', "Y"); // Yen sign
	char_map.put('\u00a6', "|");
	char_map.put('\u00a7', "S"); // Section Sign
	char_map.put('\u00a8', ""); // DIAERESIS
	char_map.put('\u00a9', "(c)");
	char_map.put('\u00aa', "a");
	char_map.put('\u00ab', "\"");
	char_map.put('\u00ac', "!"); // Not sign
	char_map.put('\u00ad', "-");
	char_map.put('\u00ae', "(r)");
	char_map.put('\u00af', "-"); // Macron
	char_map.put('\u00b0', " degrees "); // Degree
	char_map.put('\u00b1', "+/-");
	char_map.put('\u00b2', "2");
	char_map.put('\u00b3', "3");
	char_map.put('\u00b4', "'");
	char_map.put('\u00b5', "u");
	char_map.put('\u00b6', " "); // Paragraph
	char_map.put('\u00b7', "."); // Middle dot
	char_map.put('\u00b8', " "); // Cedilla
	char_map.put('\u00b9', "1");
	char_map.put('\u00ba', "o");
	char_map.put('\u00bb', "\"");
	char_map.put('\u00bc', "1/4");
	char_map.put('\u00bd', "1/2");
	char_map.put('\u00be', "3/4");
	char_map.put('\u00bf', "?"); // inverted question mark
	char_map.put('\u00c0', "A");
	char_map.put('\u00c1', "A");
	char_map.put('\u00c2', "A");
	char_map.put('\u00c3', "A");
	char_map.put('\u00c4', "A");
	char_map.put('\u00c5', "A");
	char_map.put('\u00c6', "AE");
	char_map.put('\u00c7', "C");
	char_map.put('\u00c8', "E");
	char_map.put('\u00c9', "E");
	char_map.put('\u00ca', "E");
	char_map.put('\u00cb', "E");
	char_map.put('\u00cc', "I");
	char_map.put('\u00cd', "I");
	char_map.put('\u00ce', "I");
	char_map.put('\u00cf', "I");
	char_map.put('\u00d0', "D"); // Cap Eth
	char_map.put('\u00d1', "N");
	char_map.put('\u00d2', "O");
	char_map.put('\u00d3', "O");
	char_map.put('\u00d4', "O");
	char_map.put('\u00d5', "O");
	char_map.put('\u00d6', "O");
	char_map.put('\u00d7', "*");
	char_map.put('\u00d8', "O");
	char_map.put('\u00d9', "U");
	char_map.put('\u00da', "U");
	char_map.put('\u00db', "U");
	char_map.put('\u00dc', "U");
	char_map.put('\u00dd', "Y");
	char_map.put('\u00de', "P"); // Cap Thorn
	char_map.put('\u00df', "beta");
	char_map.put('\u00e0', "a");
	char_map.put('\u00e1', "a");
	char_map.put('\u00e2', "a");
	char_map.put('\u00e3', "a");
	char_map.put('\u00e4', "a");
	char_map.put('\u00e5', "a");
	char_map.put('\u00e6', "ae");
	char_map.put('\u00e7', "c");
	char_map.put('\u00e8', "e");
	char_map.put('\u00e9', "e");
	char_map.put('\u00ea', "e");
	char_map.put('\u00eb', "e");
	char_map.put('\u00ec', "i");
	char_map.put('\u00ed', "i");
	char_map.put('\u00ee', "i");
	char_map.put('\u00ef', "i");
	char_map.put('\u00f0', "d"); // Decimal=240 (Latin Small Letter Eth)
	char_map.put('\u00f1', "n");
	char_map.put('\u00f2', "o");
	char_map.put('\u00f3', "o");
	char_map.put('\u00f4', "o");
	char_map.put('\u00f5', "o");
	char_map.put('\u00f6', "o");
	char_map.put('\u00f7', "/");
	char_map.put('\u00f8', "o");
	char_map.put('\u00f9', "u");
	char_map.put('\u00fa', "u");
	char_map.put('\u00fb', "u");
	char_map.put('\u00fc', "u");
	char_map.put('\u00fd', "y");
	char_map.put('\u00fe', "p"); // Thorn
	char_map.put('\u00ff', "y");
	char_map.put('\u0100', "A");
	char_map.put('\u0101', "a");
	char_map.put('\u0102', "A");
	char_map.put('\u0103', "a");
	char_map.put('\u0104', "A");
	char_map.put('\u0105', "a");
	char_map.put('\u0106', "C");
	char_map.put('\u0107', "c");
	char_map.put('\u0108', "C");
	char_map.put('\u0109', "c");
	char_map.put('\u010a', "C");
	char_map.put('\u010b', "c");
	char_map.put('\u010c', "C");
	char_map.put('\u010d', "c");
	char_map.put('\u010e', "D");
	char_map.put('\u010f', "d");
	char_map.put('\u0110', "D");
	char_map.put('\u0111', "d");
	char_map.put('\u0112', "E");
	char_map.put('\u0113', "e");
	char_map.put('\u0114', "E");
	char_map.put('\u0115', "e");
	char_map.put('\u0116', "E");
	char_map.put('\u0117', "e");
	char_map.put('\u0118', "E");
	char_map.put('\u0119', "e");
	char_map.put('\u011a', "E");
	char_map.put('\u011b', "e");
	char_map.put('\u011c', "G");
	char_map.put('\u011d', "g");
	char_map.put('\u011e', "G");
	char_map.put('\u011f', "g");
	char_map.put('\u0120', "G");
	char_map.put('\u0121', "g");
	char_map.put('\u0122', "G");
	char_map.put('\u0123', "g");
	char_map.put('\u0124', "H");
	char_map.put('\u0125', "h");
	char_map.put('\u0126', "H");
	char_map.put('\u0127', "h");
	char_map.put('\u0128', "I");
	char_map.put('\u0129', "i");
	char_map.put('\u012a', "I");
	char_map.put('\u012b', "i");
	char_map.put('\u012c', "I");
	char_map.put('\u012d', "i");
	char_map.put('\u012e', "I");
	char_map.put('\u012f', "i");
	char_map.put('\u0130', "I");
	char_map.put('\u0131', "i");
	char_map.put('\u0132', "IJ");
	char_map.put('\u0133', "ij");
	char_map.put('\u0134', "J");
	char_map.put('\u0135', "j");
	char_map.put('\u0136', "K");
	char_map.put('\u0137', "k");
	char_map.put('\u0139', "L");
	char_map.put('\u013a', "l");
	char_map.put('\u013b', "L");
	char_map.put('\u013c', "l");
	char_map.put('\u013d', "L");
	char_map.put('\u013e', "l");
	char_map.put('\u013f', "L");
	char_map.put('\u0140', "l");
	char_map.put('\u0141', "L");
	char_map.put('\u0142', "l");
	char_map.put('\u0143', "N");
	char_map.put('\u0144', "n");
	char_map.put('\u0145', "N");
	char_map.put('\u0146', "n");
	char_map.put('\u0147', "N");
	char_map.put('\u0148', "n");
	char_map.put('\u0149', "n");
	char_map.put('\u014b', "n");
	char_map.put('\u014c', "O");
	char_map.put('\u014d', "o");
	char_map.put('\u014e', "O");
	char_map.put('\u014f', "o");
	char_map.put('\u0151', "o");
	char_map.put('\u0152', "OE");
	char_map.put('\u0153', "oe");
	char_map.put('\u0154', "R");
	char_map.put('\u0155', "r");
	char_map.put('\u0156', "R");
	char_map.put('\u0157', "r");
	char_map.put('\u0158', "R");
	char_map.put('\u0159', "r");
	char_map.put('\u015a', "S");
	char_map.put('\u015b', "s");
	char_map.put('\u015c', "S");
	char_map.put('\u015d', "s");
	char_map.put('\u015e', "S");
	char_map.put('\u015f', "s");
	char_map.put('\u0160', "S");
	char_map.put('\u0161', "s");
	char_map.put('\u0162', "T");
	char_map.put('\u0163', "t");
	char_map.put('\u0164', "T");
	char_map.put('\u0165', "t");
	char_map.put('\u0166', "T");
	char_map.put('\u0167', "t");
	char_map.put('\u0168', "U");
	char_map.put('\u0169', "u");
	char_map.put('\u016a', "U");
	char_map.put('\u016b', "u");
	char_map.put('\u016c', "U");
	char_map.put('\u016d', "u");
	char_map.put('\u016e', "U");
	char_map.put('\u016f', "u");
	char_map.put('\u0170', "U");
	char_map.put('\u0171', "u");
	char_map.put('\u0172', "U");
	char_map.put('\u0173', "u");
	char_map.put('\u0174', "W");
	char_map.put('\u0175', "w");
	char_map.put('\u0176', "Y");
	char_map.put('\u0177', "y");
	char_map.put('\u0178', "Y");
	char_map.put('\u0179', "Z");
	char_map.put('\u017a', "z");
	char_map.put('\u017b', "Z");
	char_map.put('\u017c', "z");
	char_map.put('\u017d', "Z");
	char_map.put('\u017e', "z");
	char_map.put('\u017f', "s");
	char_map.put('\u0180', "b");
	char_map.put('\u0181', "B");
	char_map.put('\u0182', "B");
	char_map.put('\u0183', "b");
	char_map.put('\u0184', "6");
	char_map.put('\u0185', "6");
	char_map.put('\u0186', "O");
	char_map.put('\u0187', "C");
	char_map.put('\u0188', "c");
	char_map.put('\u018a', "D");
	char_map.put('\u018b', "D");
	char_map.put('\u018c', "d");
	char_map.put('\u018e', "E");
	char_map.put('\u0191', "F");
	char_map.put('\u0192', "f");
	char_map.put('\u0193', "G");
	char_map.put('\u0194', "Gamma");
	char_map.put('\u0195', "hv");
	char_map.put('\u0196', "Iota");
	char_map.put('\u0197', "I");
	char_map.put('\u0198', "K");
	char_map.put('\u0199', "k");
	char_map.put('\u019a', "l");
	char_map.put('\u019b', "lambda");
	char_map.put('\u019c', "M");
	char_map.put('\u019d', "N");
	char_map.put('\u019e', "n");
	char_map.put('\u019f', "O");
	char_map.put('\u01a0', "O");
	char_map.put('\u01a1', "o");
	char_map.put('\u01a2', "OI");
	char_map.put('\u01a3', "oi");
	char_map.put('\u01a4', "P");
	char_map.put('\u01a5', "p");
	char_map.put('\u01a6', "YR");
	char_map.put('\u01a7', "2");
	char_map.put('\u01a8', "2");
	char_map.put('\u01ab', "t");
	char_map.put('\u01ac', "T");
	char_map.put('\u01ad', "t");
	char_map.put('\u01ae', "T");
	char_map.put('\u01af', "U");
	char_map.put('\u01b0', "u");
	char_map.put('\u01b1', "Upsilon");
	char_map.put('\u01b2', "V");
	char_map.put('\u01b3', "Y");
	char_map.put('\u01b4', "y");
	char_map.put('\u01b5', "Z");
	char_map.put('\u01b6', "z");
	char_map.put('\u01bb', "2");
	char_map.put('\u01bc', "5");
	char_map.put('\u01bd', "5");
	char_map.put('\u01c0', "|");
	char_map.put('\u01c3', "!");
	char_map.put('\u01c4', "DZ");
	char_map.put('\u01c5', "Dz");
	char_map.put('\u01c6', "dz");
	char_map.put('\u01c7', "LJ");
	char_map.put('\u01c8', "Lj");
	char_map.put('\u01c9', "lj");
	char_map.put('\u01ca', "NJ");
	char_map.put('\u01cb', "Nj");
	char_map.put('\u01cc', "nj");
	char_map.put('\u01cd', "A");
	char_map.put('\u01ce', "a");
	char_map.put('\u01cf', "I");
	char_map.put('\u01d0', "i");
	char_map.put('\u01d1', "O");
	char_map.put('\u01d2', "o");
	char_map.put('\u01d3', "U");
	char_map.put('\u01d4', "u");
	char_map.put('\u01d5', "U");
	char_map.put('\u01d6', "u");
	char_map.put('\u01d7', "U");
	char_map.put('\u01d8', "u");
	char_map.put('\u01d9', "U");
	char_map.put('\u01da', "u");
	char_map.put('\u01db', "U");
	char_map.put('\u01dc', "u");
	char_map.put('\u01dd', "e");
	char_map.put('\u01de', "A");
	char_map.put('\u01df', "a");
	char_map.put('\u01e0', "A");
	char_map.put('\u01e1', "a");
	char_map.put('\u01e2', "AE");
	char_map.put('\u01e3', "ae");
	char_map.put('\u01e4', "G");
	char_map.put('\u01e5', "g");
	char_map.put('\u01e6', "G");
	char_map.put('\u01e7', "g");
	char_map.put('\u01e8', "K");
	char_map.put('\u01e9', "k");
	char_map.put('\u01ea', "O");
	char_map.put('\u01eb', "o");
	char_map.put('\u01ec', "O");
	char_map.put('\u01ed', "o");
	char_map.put('\u01f0', "j");
	char_map.put('\u01f1', "DZ");
	char_map.put('\u01f2', "Dz");
	char_map.put('\u01f3', "dz");
	char_map.put('\u01f4', "G");
	char_map.put('\u01f5', "g");
	char_map.put('\u01f8', "N");
	char_map.put('\u01f9', "n");
	char_map.put('\u01fa', "A");
	char_map.put('\u01fb', "a");
	char_map.put('\u01fc', "AE");
	char_map.put('\u01fd', "ae");
	char_map.put('\u01fe', "O");
	char_map.put('\u01ff', "o");
	char_map.put('\u0200', "A");
	char_map.put('\u0201', "a");
	char_map.put('\u0202', "A");
	char_map.put('\u0203', "a");
	char_map.put('\u0204', "E");
	char_map.put('\u0205', "e");
	char_map.put('\u0206', "E");
	char_map.put('\u0207', "e");
	char_map.put('\u0208', "I");
	char_map.put('\u0209', "i");
	char_map.put('\u020a', "I");
	char_map.put('\u020b', "i");
	char_map.put('\u020c', "O");
	char_map.put('\u020d', "o");
	char_map.put('\u020e', "O");
	char_map.put('\u020f', "o");
	char_map.put('\u0210', "R");
	char_map.put('\u0211', "r");
	char_map.put('\u0212', "R");
	char_map.put('\u0213', "r");
	char_map.put('\u0214', "U");
	char_map.put('\u0215', "u");
	char_map.put('\u0216', "U");
	char_map.put('\u0217', "u");
	char_map.put('\u0218', "S");
	char_map.put('\u0219', "s");
	char_map.put('\u021a', "T");
	char_map.put('\u021b', "t");
	char_map.put('\u021e', "H");
	char_map.put('\u021f', "h");
	char_map.put('\u0220', "N");
	char_map.put('\u0221', "d");
	char_map.put('\u0222', "OU");
	char_map.put('\u0223', "ou");
	char_map.put('\u0224', "Z");
	char_map.put('\u0225', "z");
	char_map.put('\u0226', "A");
	char_map.put('\u0227', "a");
	char_map.put('\u0228', "E");
	char_map.put('\u0229', "e");
	char_map.put('\u022a', "O");
	char_map.put('\u022b', "o");
	char_map.put('\u022c', "O");
	char_map.put('\u022d', "o");
	char_map.put('\u022e', "O");
	char_map.put('\u022f', "o");
	char_map.put('\u0230', "O");
	char_map.put('\u0231', "o");
	char_map.put('\u0232', "Y");
	char_map.put('\u0233', "y");
	char_map.put('\u0234', "l");
	char_map.put('\u0235', "n");
	char_map.put('\u0236', "t");
	char_map.put('\u0237', "j");
	char_map.put('\u0238', "db");
	char_map.put('\u0239', "qp");
	char_map.put('\u023a', "A");
	char_map.put('\u023b', "C");
	char_map.put('\u023c', "c");
	char_map.put('\u023d', "L");
	char_map.put('\u023e', "T");
	char_map.put('\u023f', "s");
	char_map.put('\u0240', "z");
	char_map.put('\u0243', "B");
	char_map.put('\u0244', "U");
	char_map.put('\u0245', "V");
	char_map.put('\u0246', "E");
	char_map.put('\u0247', "e");
	char_map.put('\u0248', "J");
	char_map.put('\u0249', "j");
	char_map.put('\u024a', "Q");
	char_map.put('\u024b', "q");
	char_map.put('\u024c', "R");
	char_map.put('\u024d', "r");
	char_map.put('\u024e', "Y");
	char_map.put('\u024f', "y");
	char_map.put('\u0250', "a");
	char_map.put('\u0251', "alpha");
	char_map.put('\u0252', "alpha");
	char_map.put('\u0253', "b");
	char_map.put('\u0254', "o");
	char_map.put('\u0255', "c");
	char_map.put('\u0256', "d");
	char_map.put('\u0257', "d");
	char_map.put('\u0259', "e");
	char_map.put('\u025b', "E");
	char_map.put('\u025f', "j");
	char_map.put('\u0260', "g");
	char_map.put('\u0261', "g");
	char_map.put('\u0262', "G");
	char_map.put('\u0263', "gamma");
	char_map.put('\u0265', "h");
	char_map.put('\u0266', "h");
	char_map.put('\u0268', "i");
	char_map.put('\u0269', "iota");
	char_map.put('\u026a', "i");
	char_map.put('\u026b', "l");
	char_map.put('\u026c', "l");
	char_map.put('\u026d', "l");
	char_map.put('\u026f', "m");
	char_map.put('\u0270', "m");
	char_map.put('\u0271', "m");
	char_map.put('\u0272', "n");
	char_map.put('\u0273', "n");
	char_map.put('\u0274', "N");
	char_map.put('\u0275', "o");
	char_map.put('\u0276', "OE");
	char_map.put('\u0277', "omega");
	char_map.put('\u0278', "phi");
	char_map.put('\u0279', "r");
	char_map.put('\u027a', "r");
	char_map.put('\u027b', "r");
	char_map.put('\u027c', "r");
	char_map.put('\u027d', "r");
	char_map.put('\u027e', "r");
	char_map.put('\u0280', "R");
	char_map.put('\u0282', "s");
	char_map.put('\u0284', "j");
	char_map.put('\u0287', "t");
	char_map.put('\u0288', "t");
	char_map.put('\u0289', "u");
	char_map.put('\u028a', "upsilon");
	char_map.put('\u028b', "v");
	char_map.put('\u028c', "v");
	char_map.put('\u028d', "w");
	char_map.put('\u028e', "y");
	char_map.put('\u028f', "Y");
	char_map.put('\u0290', "z");
	char_map.put('\u0291', "z");
	char_map.put('\u0297', "C");
	char_map.put('\u0299', "B");
	char_map.put('\u029a', "e");
	char_map.put('\u029b', "G");
	char_map.put('\u029c', "H");
	char_map.put('\u029d', "j");
	char_map.put('\u029e', "k");
	char_map.put('\u029f', "L");
	char_map.put('\u02a0', "q");
	char_map.put('\u02a3', "DZ");
	char_map.put('\u02a5', "dz");
	char_map.put('\u02a6', "ts");
	char_map.put('\u02a8', "tc");
	char_map.put('\u02aa', "ls");
	char_map.put('\u02ab', "lz");
	char_map.put('\u02ae', "h");
	char_map.put('\u02af', "h");
	char_map.put('\u02b0', "h");
	char_map.put('\u02b1', "h");
	char_map.put('\u02b2', "j");
	char_map.put('\u02b3', "r");
	char_map.put('\u02b4', "r");
	char_map.put('\u02b5', "r");
	char_map.put('\u02b6', "r");
	char_map.put('\u02b7', "w");
	char_map.put('\u02b8', "y");
	char_map.put('\u02b9', "'");
	char_map.put('\u02ba', "\"");
	char_map.put('\u02bb', "'");
	char_map.put('\u02bc', "'");
	char_map.put('\u02bd', "'");
	char_map.put('\u02be', "'");
	char_map.put('\u02c6', "^");
	char_map.put('\u02c7', "^");
	char_map.put('\u02d8', "");
	char_map.put('\u02d9', "");
	char_map.put('\u02da', "");
	char_map.put('\u02db', "");
	char_map.put('\u02dc', "~");
	char_map.put('\u02dd', "\"");
	char_map.put('\u02c7', "");

	char_map.put('\u0363', "a"); // FML 2017/06/08; http://www.fileformat.info/info/unicode/char/0363/index.htm
	char_map.put('\u037e', ";");
	char_map.put('\u0386', "Alpha");
	char_map.put('\u0388', "Epsilon");
	char_map.put('\u0389', "Eta");
	char_map.put('\u038a', "Iota");
	char_map.put('\u038c', "Omicron");
	char_map.put('\u038e', "Upsilon");
	char_map.put('\u038f', "Omega");
	char_map.put('\u0390', "iota");
	char_map.put('\u0391', "Alpha");
	char_map.put('\u0392', "Beta");
	char_map.put('\u0393', "Gamma");
	char_map.put('\u0394', "Delta");
	char_map.put('\u0395', "Epsilon");
	char_map.put('\u0396', "Zeta");
	char_map.put('\u0397', "Eta");
	char_map.put('\u0398', "Theta");
	char_map.put('\u0399', "Iota");
	char_map.put('\u039a', "Kappa");
	char_map.put('\u039b', "Lambda");
	char_map.put('\u039c', "MU");
	char_map.put('\u039d', "Nu");
	char_map.put('\u039e', "Xi");
	char_map.put('\u039f', "Omicron");
	char_map.put('\u03a0', "Pi");
	char_map.put('\u03a1', "Rho");
	char_map.put('\u03a3', "Sigma");
	char_map.put('\u03a4', "Tau");
	char_map.put('\u03a5', "Upsilon");
	char_map.put('\u03a6', "Phi");
	char_map.put('\u03a7', "Chi");
	char_map.put('\u03a8', "Psi");
	char_map.put('\u03a9', "Omega");
	char_map.put('\u03aa', "Iota");
	char_map.put('\u03ab', "Upsilon");
	char_map.put('\u03ac', "alpha");
	char_map.put('\u03ad', "epsilon");
	char_map.put('\u03ae', "eta");
	char_map.put('\u03af', "iota");
	char_map.put('\u03b0', "upsilon");
	char_map.put('\u03b1', "alpha");
	char_map.put('\u03b2', "beta");
	char_map.put('\u03b3', "gamma");
	char_map.put('\u03b4', "delta");
	char_map.put('\u03b5', "epsilon");
	char_map.put('\u03b6', "zeta");
	char_map.put('\u03b7', "eta");
	char_map.put('\u03b8', "theta");
	char_map.put('\u03b9', "iota");
	char_map.put('\u03ba', "kappa");
	char_map.put('\u03bb', "lambda");
	char_map.put('\u03bc', "MU");
	char_map.put('\u03bd', "nu");
	char_map.put('\u03be', "xi");
	char_map.put('\u03bf', "omicron");
	char_map.put('\u03c0', "pi");
	char_map.put('\u03c1', "rho");
	char_map.put('\u03c2', "sigma");
	char_map.put('\u03c3', "sigma");
	char_map.put('\u03c4', "tau");
	char_map.put('\u03c5', "upsilon");
	char_map.put('\u03c6', "phi");
	char_map.put('\u03c7', "chi");
	char_map.put('\u03c8', "psi");
	char_map.put('\u03c9', "omega");
	char_map.put('\u03ca', "iota");
	char_map.put('\u03cb', "upsilon");
	char_map.put('\u03cc', "omicron");
	char_map.put('\u03cd', "upsilon");
	char_map.put('\u03ce', "omega");
	char_map.put('\u03d0', "beta");
	char_map.put('\u03d1', "theta");
	char_map.put('\u03d2', "Upsilon");
	char_map.put('\u03d3', "Upsilon");
	char_map.put('\u03d4', "Upsilon");
	char_map.put('\u03d5', "phi");
	char_map.put('\u03d6', "pi");
	char_map.put('\u03f0', "kappa");
	char_map.put('\u03f1', "rho");
	char_map.put('\u03f2', "sigma");
	char_map.put('\u03f4', "Theta");
	char_map.put('\u03f5', "epsilon");
	char_map.put('\u03f9', "Sigma");
	char_map.put('\u03fc', "Rho");
	char_map.put('\u1d00', "A"); // FML 2016/06/08; http://www.fileformat.info/info/unicode/char/1d00/index.htm
	char_map.put('\u1d01', "AE"); // FML 2016/06/08; http://www.fileformat.info/info/unicode/char/1d01/index.htm
	char_map.put('\u1d02', "ae"); // FML 2016/06/08; http://www.fileformat.info/info/unicode/char/1d02/index.htm
	char_map.put('\u1d43', "a");
	char_map.put('\u1d9c', "c");
	char_map.put('\u1e00', "A");
	char_map.put('\u1e01', "a");
	char_map.put('\u1e02', "B");
	char_map.put('\u1e03', "b");
	char_map.put('\u1e04', "B");
	char_map.put('\u1e05', "b");
	char_map.put('\u1e06', "B");
	char_map.put('\u1e07', "b");
	char_map.put('\u1e08', "C");
	char_map.put('\u1e09', "c");
	char_map.put('\u1e0a', "D");
	char_map.put('\u1e0b', "d");
	char_map.put('\u1e0c', "D");
	char_map.put('\u1e0d', "d");
	char_map.put('\u1e0e', "D");
	char_map.put('\u1e0f', "d");
	char_map.put('\u1e10', "D");
	char_map.put('\u1e11', "d");
	char_map.put('\u1e12', "D");
	char_map.put('\u1e13', "d");
	char_map.put('\u1e14', "E");
	char_map.put('\u1e15', "e");
	char_map.put('\u1e16', "E");
	char_map.put('\u1e17', "e");
	char_map.put('\u1e18', "E");
	char_map.put('\u1e19', "e");
	char_map.put('\u1e1a', "E");
	char_map.put('\u1e1b', "e");
	char_map.put('\u1e1c', "E");
	char_map.put('\u1e1d', "e");
	char_map.put('\u1e1e', "F");
	char_map.put('\u1e1f', "f");
	char_map.put('\u1e20', "G");
	char_map.put('\u1e21', "g");
	char_map.put('\u1e22', "H");
	char_map.put('\u1e23', "h");
	char_map.put('\u1e24', "H");
	char_map.put('\u1e25', "h");
	char_map.put('\u1e26', "H");
	char_map.put('\u1e27', "h");
	char_map.put('\u1e28', "H");
	char_map.put('\u1e29', "h");
	char_map.put('\u1e2a', "H");
	char_map.put('\u1e2b', "h");
	char_map.put('\u1e2c', "I");
	char_map.put('\u1e2d', "i");
	char_map.put('\u1e2e', "I");
	char_map.put('\u1e2f', "i");
	char_map.put('\u1e30', "K");
	char_map.put('\u1e31', "k");
	char_map.put('\u1e32', "K");
	char_map.put('\u1e33', "k");
	char_map.put('\u1e34', "K");
	char_map.put('\u1e35', "k");
	char_map.put('\u1e36', "L");
	char_map.put('\u1e37', "l");
	char_map.put('\u1e38', "L");
	char_map.put('\u1e39', "l");
	char_map.put('\u1e3a', "L");
	char_map.put('\u1e3b', "l");
	char_map.put('\u1e3c', "L");
	char_map.put('\u1e3d', "l");
	char_map.put('\u1e3e', "M");
	char_map.put('\u1e3f', "m");
	char_map.put('\u1e40', "M");
	char_map.put('\u1e41', "m");
	char_map.put('\u1e42', "M");
	char_map.put('\u1e43', "m");
	char_map.put('\u1e44', "N");
	char_map.put('\u1e45', "n");
	char_map.put('\u1e46', "N");
	char_map.put('\u1e47', "n");
	char_map.put('\u1e48', "N");
	char_map.put('\u1e49', "n");
	char_map.put('\u1e4a', "N");
	char_map.put('\u1e4b', "n");
	char_map.put('\u1e4c', "O");
	char_map.put('\u1e4d', "o");
	char_map.put('\u1e4e', "O");
	char_map.put('\u1e4f', "o");
	char_map.put('\u1e50', "O");
	char_map.put('\u1e51', "o");
	char_map.put('\u1e52', "O");
	char_map.put('\u1e53', "o");
	char_map.put('\u1e54', "P");
	char_map.put('\u1e55', "p");
	char_map.put('\u1e56', "P");
	char_map.put('\u1e57', "p");
	char_map.put('\u1e58', "R");
	char_map.put('\u1e59', "r");
	char_map.put('\u1e5a', "R");
	char_map.put('\u1e5b', "r");
	char_map.put('\u1e5c', "R");
	char_map.put('\u1e5d', "r");
	char_map.put('\u1e5e', "R");
	char_map.put('\u1e5f', "r");
	char_map.put('\u1e60', "S");
	char_map.put('\u1e61', "s");
	char_map.put('\u1e62', "S");
	char_map.put('\u1e63', "s");
	char_map.put('\u1e64', "S");
	char_map.put('\u1e65', "s");
	char_map.put('\u1e66', "S");
	char_map.put('\u1e67', "s");
	char_map.put('\u1e68', "S");
	char_map.put('\u1e69', "s");
	char_map.put('\u1e6a', "T");
	char_map.put('\u1e6b', "t");
	char_map.put('\u1e6c', "T");
	char_map.put('\u1e6d', "t");
	char_map.put('\u1e6e', "T");
	char_map.put('\u1e6f', "t");
	char_map.put('\u1e70', "T");
	char_map.put('\u1e71', "t");
	char_map.put('\u1e72', "U");
	char_map.put('\u1e73', "u");
	char_map.put('\u1e74', "U");
	char_map.put('\u1e75', "u");
	char_map.put('\u1e76', "U");
	char_map.put('\u1e77', "u");
	char_map.put('\u1e78', "U");
	char_map.put('\u1e79', "u");
	char_map.put('\u1e7a', "U");
	char_map.put('\u1e7b', "u");
	char_map.put('\u1e7c', "V");
	char_map.put('\u1e7d', "v");
	char_map.put('\u1e7e', "V");
	char_map.put('\u1e7f', "v");
	char_map.put('\u1e80', "W");
	char_map.put('\u1e81', "w");
	char_map.put('\u1e82', "W");
	char_map.put('\u1e83', "w");
	char_map.put('\u1e84', "W");
	char_map.put('\u1e85', "w");
	char_map.put('\u1e86', "W");
	char_map.put('\u1e87', "w");
	char_map.put('\u1e88', "W");
	char_map.put('\u1e89', "w");
	char_map.put('\u1e8a', "X");
	char_map.put('\u1e8b', "x");
	char_map.put('\u1e8c', "X");
	char_map.put('\u1e8d', "x");
	char_map.put('\u1e8e', "Y");
	char_map.put('\u1e8f', "y");
	char_map.put('\u1e90', "Z");
	char_map.put('\u1e91', "z");
	char_map.put('\u1e92', "Z");
	char_map.put('\u1e93', "z");
	char_map.put('\u1e94', "Z");
	char_map.put('\u1e95', "z");
	char_map.put('\u1e96', "h");
	char_map.put('\u1e97', "t");
	char_map.put('\u1e98', "w");
	char_map.put('\u1e99', "y");
	char_map.put('\u1e9a', "a");
	char_map.put('\u1e9b', "s");
	char_map.put('\u1ea0', "A");
	char_map.put('\u1ea1', "a");
	char_map.put('\u1ea2', "A");
	char_map.put('\u1ea3', "a");
	char_map.put('\u1ea4', "A");
	char_map.put('\u1ea5', "a");
	char_map.put('\u1ea6', "A");
	char_map.put('\u1ea7', "a");
	char_map.put('\u1ea8', "A");
	char_map.put('\u1ea9', "a");
	char_map.put('\u1eaa', "A");
	char_map.put('\u1eab', "a");
	char_map.put('\u1eac', "A");
	char_map.put('\u1ead', "a");
	char_map.put('\u1eae', "A");
	char_map.put('\u1eaf', "a");
	char_map.put('\u1eb0', "A");
	char_map.put('\u1eb1', "a");
	char_map.put('\u1eb2', "A");
	char_map.put('\u1eb3', "a");
	char_map.put('\u1eb4', "A");
	char_map.put('\u1eb5', "a");
	char_map.put('\u1eb6', "A");
	char_map.put('\u1eb7', "a");
	char_map.put('\u1eb8', "E");
	char_map.put('\u1eb9', "e");
	char_map.put('\u1eba', "E");
	char_map.put('\u1ebb', "e");
	char_map.put('\u1ebc', "E");
	char_map.put('\u1ebd', "e");
	char_map.put('\u1ebe', "E");
	char_map.put('\u1ebf', "e");
	char_map.put('\u1ec0', "E");
	char_map.put('\u1ec1', "e");
	char_map.put('\u1ec2', "E");
	char_map.put('\u1ec3', "e");
	char_map.put('\u1ec4', "E");
	char_map.put('\u1ec5', "e");
	char_map.put('\u1ec6', "E");
	char_map.put('\u1ec7', "e");
	char_map.put('\u1ec8', "I");
	char_map.put('\u1ec9', "i");
	char_map.put('\u1eca', "I");
	char_map.put('\u1ecb', "i");
	char_map.put('\u1ecc', "O");
	char_map.put('\u1ecd', "o");
	char_map.put('\u1ece', "O");
	char_map.put('\u1ecf', "o");
	char_map.put('\u1ed0', "O");
	char_map.put('\u1ed1', "o");
	char_map.put('\u1ed2', "O");
	char_map.put('\u1ed3', "o");
	char_map.put('\u1ed4', "O");
	char_map.put('\u1ed5', "o");
	char_map.put('\u1ed6', "O");
	char_map.put('\u1ed7', "o");
	char_map.put('\u1ed8', "O");
	char_map.put('\u1ed9', "o");
	char_map.put('\u1eda', "O");
	char_map.put('\u1edb', "o");
	char_map.put('\u1edc', "O");
	char_map.put('\u1edd', "o");
	char_map.put('\u1ede', "O");
	char_map.put('\u1edf', "o");
	char_map.put('\u1ee0', "O");
	char_map.put('\u1ee1', "o");
	char_map.put('\u1ee2', "O");
	char_map.put('\u1ee3', "o");
	char_map.put('\u1ee4', "U");
	char_map.put('\u1ee5', "u");
	char_map.put('\u1ee6', "U");
	char_map.put('\u1ee7', "u");
	char_map.put('\u1ee8', "U");
	char_map.put('\u1ee9', "u");
	char_map.put('\u1eea', "U");
	char_map.put('\u1eeb', "u");
	char_map.put('\u1eec', "U");
	char_map.put('\u1eed', "u");
	char_map.put('\u1eee', "U");
	char_map.put('\u1eef', "u");
	char_map.put('\u1ef0', "U");
	char_map.put('\u1ef1', "u");
	char_map.put('\u1ef2', "Y");
	char_map.put('\u1ef3', "y");
	char_map.put('\u1ef4', "Y");
	char_map.put('\u1ef5', "y");
	char_map.put('\u1ef6', "Y");
	char_map.put('\u1ef7', "y");
	char_map.put('\u1ef8', "Y");
	char_map.put('\u1ef9', "y");
	char_map.put('\u1f77', "i");
	char_map.put('\u1fc6', "n");
	char_map.put('\u2002', " ");
	char_map.put('\u2003', " ");
	char_map.put('\u2009', " ");
	char_map.put('\u2010', "-");
	char_map.put('\u2011', "-");
	char_map.put('\u2012', "-");
	char_map.put('\u2013', "-");
	char_map.put('\u2014', "-");
	char_map.put('\u2015', "--");
	char_map.put('\u2016', "||");
	char_map.put('\u2017', "_");
	char_map.put('\u2018', "'");
	char_map.put('\u2019', "'");
	char_map.put('\u201a', ",");
	char_map.put('\u201b', "'");
	char_map.put('\u201c', "\"");
	char_map.put('\u201d', "\"");
	char_map.put('\u201e', "\"");
	char_map.put('\u201f', "\"");
	char_map.put('\u2024', ".");
	char_map.put('\u2025', "..");
	char_map.put('\u2026', "...");
	char_map.put('\u202f', " ");
	char_map.put('\u2030', "0/00");
	char_map.put('\u2031', "0/000");
	char_map.put('\u2032', "'");
	char_map.put('\u2033', "\"");
	char_map.put('\u2034', "'''");
	char_map.put('\u2035', "'");
	char_map.put('\u2036', "\"");
	char_map.put('\u2037', "'''");
	char_map.put('\u2038', "^");
	char_map.put('\u2039', "&lt;");
	char_map.put('\u203a', ">");
	char_map.put('\u203c', "!!");
	char_map.put('\u203d', "?");
	char_map.put('\u2044', "/");
	char_map.put('\u2045', "[");
	char_map.put('\u2046', "]");
	char_map.put('\u2047', "??");
	char_map.put('\u2048', "?!");
	char_map.put('\u2049', "!?");
	char_map.put('\u204e', "*");
	char_map.put('\u2052', "%");
	char_map.put('\u2053', "~");
	char_map.put('\u2056', "...");
	char_map.put('\u2057', "''''");
	char_map.put('\u2058', "....");
	char_map.put('\u2059', ".....");
	char_map.put('\u205a', "..");
	char_map.put('\u205f', " ");

	char_map.put('\u2070', "0"); // superscript 0
	char_map.put('\u2071', "i");
	char_map.put('\u2072', "2");
	char_map.put('\u2073', "3");
	char_map.put('\u2074', "4");
	char_map.put('\u2075', "5");
	char_map.put('\u2076', "6");
	char_map.put('\u2077', "7");
	char_map.put('\u2078', "8");
	char_map.put('\u2079', "9");
	char_map.put('\u207A', "+");
	char_map.put('\u207B', "-");
	char_map.put('\u207C', "=");
	char_map.put('\u207D', "(");
	char_map.put('\u207E', ")");
	char_map.put('\u207F', "n");

	char_map.put('\u2080', "0"); // subscript 0
	char_map.put('\u2081', "1"); // subscript 1
	char_map.put('\u2082', "2"); // subscript 2
	char_map.put('\u2083', "3"); // subscript 3
	char_map.put('\u2084', "4"); // subscript 4
	char_map.put('\u2085', "5"); // subscript 5
	char_map.put('\u2086', "6"); // subscript 6
	char_map.put('\u2087', "7"); // subscript 7
	char_map.put('\u2088', "8"); // subscript 8
	char_map.put('\u2089', "9"); // subscript 9
	char_map.put('\u208A', "+");
	char_map.put('\u208B', "-");
	char_map.put('\u208C', "=");
	char_map.put('\u208D', "(");
	char_map.put('\u208E', ")");
	char_map.put('\u2090', "a");
	char_map.put('\u2091', "e");
	char_map.put('\u2092', "o");
	char_map.put('\u2093', "x");
	char_map.put('\u2094', "e"); // subscript schwa
	char_map.put('\u2095', "h");
	char_map.put('\u2096', "k");
	char_map.put('\u2097', "l");
	char_map.put('\u2098', "m");
	char_map.put('\u2099', "n");
	char_map.put('\u209A', "p");
	char_map.put('\u209B', "s");
	char_map.put('\u209C', "t");

	char_map.put('\u2100', "a/c");
	char_map.put('\u2101', "a/s");
	char_map.put('\u2102', "C");
	char_map.put('\u2103', "C");
	char_map.put('\u2105', "c/o");
	char_map.put('\u2106', "c/u");
	char_map.put('\u2107', "E");
	char_map.put('\u2109', "F");
	char_map.put('\u210a', "g");
	char_map.put('\u210b', "H");
	char_map.put('\u210c', "H");
	char_map.put('\u210d', "H");
	char_map.put('\u210e', "h");
	char_map.put('\u210f', "h");
	char_map.put('\u2110', "I");
	char_map.put('\u2111', "I");
	char_map.put('\u2112', "L");
	char_map.put('\u2113', "l");
	char_map.put('\u2115', "N");
	char_map.put('\u2116', "No");
	char_map.put('\u2117', "(p)");
	char_map.put('\u2118', "P");
	char_map.put('\u2119', "P");
	char_map.put('\u211a', "Q");
	char_map.put('\u211b', "R");
	char_map.put('\u211c', "R");
	char_map.put('\u211d', "R");
	char_map.put('\u211e', "Px");
	char_map.put('\u2120', "SM");
	char_map.put('\u2121', "TEL");
	char_map.put('\u2122', "TM");
	char_map.put('\u2124', "Z");
	char_map.put('\u2126', "Omega");
	char_map.put('\u2128', "Z");
	char_map.put('\u2129', "iota");
	char_map.put('\u212a', "K");
	char_map.put('\u212b', "A");
	char_map.put('\u212c', "B");
	char_map.put('\u212d', "C");
	char_map.put('\u212e', "e");
	char_map.put('\u212f', "e");
	char_map.put('\u2130', "E");
	char_map.put('\u2131', "F");
	char_map.put('\u2132', "F");
	char_map.put('\u2133', "M");
	char_map.put('\u2134', "o");
	char_map.put('\u2139', "i");
	char_map.put('\u213a', "Q");
	char_map.put('\u213b', "FAX");
	char_map.put('\u213c', "pi");
	char_map.put('\u213d', "gamma");
	char_map.put('\u213e', "Gamma");
	char_map.put('\u213f', "PI");
	char_map.put('\u2141', "G");
	char_map.put('\u2142', "L");
	char_map.put('\u2144', "Y");
	char_map.put('\u2145', "D");
	char_map.put('\u2146', "d");
	char_map.put('\u2147', "e");
	char_map.put('\u2148', "i");
	char_map.put('\u2149', "j");
	char_map.put('\u214b', "&");
	char_map.put('\u2153', "1/3");
	char_map.put('\u2154', "2/3");
	char_map.put('\u2155', "1/5");
	char_map.put('\u2156', "2/5");
	char_map.put('\u2157', "3/5");
	char_map.put('\u2158', "4/5");
	char_map.put('\u2159', "1/6");
	char_map.put('\u215a', "5/6");
	char_map.put('\u215b', "1/8");
	char_map.put('\u215c', "3/8");
	char_map.put('\u215d', "5/8");
	char_map.put('\u215e', "7/8");
	char_map.put('\u215f', "1/");
	char_map.put('\u2160', "I");
	char_map.put('\u2161', "II");
	char_map.put('\u2162', "III");
	char_map.put('\u2163', "IV");
	char_map.put('\u2164', "V");
	char_map.put('\u2165', "VI");
	char_map.put('\u2166', "VII");
	char_map.put('\u2167', "VIII");
	char_map.put('\u2168', "IX");
	char_map.put('\u2169', "X");
	char_map.put('\u216a', "XI");
	char_map.put('\u216b', "XII");
	char_map.put('\u216c', "L");
	char_map.put('\u216d', "C");
	char_map.put('\u216e', "D");
	char_map.put('\u216f', "M");
	char_map.put('\u2170', "i");
	char_map.put('\u2171', "ii");
	char_map.put('\u2172', "iii");
	char_map.put('\u2173', "iv");
	char_map.put('\u2174', "v");
	char_map.put('\u2175', "vi");
	char_map.put('\u2176', "vii");
	char_map.put('\u2177', "viii");
	char_map.put('\u2178', "ix");
	char_map.put('\u2179', "x");
	char_map.put('\u217a', "xi");
	char_map.put('\u217b', "xii");
	char_map.put('\u217c', "l");
	char_map.put('\u217d', "c");
	char_map.put('\u217e', "d");
	char_map.put('\u217f', "m");
	char_map.put('\u2180', "CD");
	char_map.put('\u2190', "&lt;-");
	char_map.put('\u2192', "->");
	char_map.put('\u2194', "&lt;->");
	char_map.put('\u219a', "&lt;-");
	char_map.put('\u219b', "->");
	char_map.put('\u21ae', "&lt;->");
	char_map.put('\u21cd', "&lt;=");
	char_map.put('\u21ce', "&lt;=>");
	char_map.put('\u21cf', "=>");
	char_map.put('\u21d0', "&lt;=");
	char_map.put('\u21d2', "=>");
	char_map.put('\u21d4', "&lt;=>");
	char_map.put('\u2303', "^");
	char_map.put('\u2329', "&lt;");
	char_map.put('\u232a', ">");
	char_map.put('\u239b', "(");
	char_map.put('\u239c', "(");
	char_map.put('\u239d', "(");
	char_map.put('\u239e', ")");
	char_map.put('\u239f', ")");
	char_map.put('\u23a0', ")");
	char_map.put('\u23a1', "[");
	char_map.put('\u23a2', "[");
	char_map.put('\u23a3', "[");
	char_map.put('\u23a4', "]");
	char_map.put('\u23a5', "]");
	char_map.put('\u23a6', "]");
	char_map.put('\u23a7', "{");
	char_map.put('\u23a8', "{");
	char_map.put('\u23a9', "{");
	char_map.put('\u23ab', "}");
	char_map.put('\u23ac', "}");
	char_map.put('\u23ad', "}");
	char_map.put('\u2460', "1");
	char_map.put('\u2461', "2");
	char_map.put('\u2462', "3");
	char_map.put('\u2463', "4");
	char_map.put('\u2464', "5");
	char_map.put('\u2465', "6");
	char_map.put('\u2466', "7");
	char_map.put('\u2467', "8");
	char_map.put('\u2468', "9");
	char_map.put('\u2469', "10");
	char_map.put('\u246a', "11");
	char_map.put('\u246b', "12");
	char_map.put('\u246c', "13");
	char_map.put('\u246d', "14");
	char_map.put('\u246e', "15");
	char_map.put('\u246f', "16");
	char_map.put('\u2470', "17");
	char_map.put('\u2471', "18");
	char_map.put('\u2472', "19");
	char_map.put('\u2473', "20");
	char_map.put('\u2474', "(1)");
	char_map.put('\u2475', "(2)");
	char_map.put('\u2476', "(3)");
	char_map.put('\u2477', "(4)");
	char_map.put('\u2478', "(5)");
	char_map.put('\u2479', "(6)");
	char_map.put('\u247a', "(7)");
	char_map.put('\u247b', "(8)");
	char_map.put('\u247c', "(9)");
	char_map.put('\u247d', "(10)");
	char_map.put('\u247e', "(11)");
	char_map.put('\u247f', "(12)");
	char_map.put('\u2480', "(13)");
	char_map.put('\u2481', "(14)");
	char_map.put('\u2482', "(15)");
	char_map.put('\u2483', "(16)");
	char_map.put('\u2484', "(17)");
	char_map.put('\u2485', "(18)");
	char_map.put('\u2486', "(19)");
	char_map.put('\u2487', "(20)");
	char_map.put('\u2488', "1.");
	char_map.put('\u2489', "2.");
	char_map.put('\u248a', "3.");
	char_map.put('\u248b', "4.");
	char_map.put('\u248c', "5.");
	char_map.put('\u248d', "6.");
	char_map.put('\u248e', "7.");
	char_map.put('\u248f', "8.");
	char_map.put('\u2490', "9.");
	char_map.put('\u2491', "10.");
	char_map.put('\u2492', "11.");
	char_map.put('\u2493', "12.");
	char_map.put('\u2494', "13.");
	char_map.put('\u2495', "14.");
	char_map.put('\u2496', "15.");
	char_map.put('\u2497', "16.");
	char_map.put('\u2498', "17.");
	char_map.put('\u2499', "18.");
	char_map.put('\u249a', "19.");
	char_map.put('\u249b', "20.");
	char_map.put('\u249c', "(a)");
	char_map.put('\u249d', "(b)");
	char_map.put('\u249e', "(c)");
	char_map.put('\u249f', "(d)");
	char_map.put('\u24a0', "(e)");
	char_map.put('\u24a1', "(f)");
	char_map.put('\u24a2', "(g)");
	char_map.put('\u24a3', "(h)");
	char_map.put('\u24a4', "(i)");
	char_map.put('\u24a5', "(j)");
	char_map.put('\u24a6', "(k)");
	char_map.put('\u24a7', "(l)");
	char_map.put('\u24a8', "(m)");
	char_map.put('\u24a9', "(n)");
	char_map.put('\u24aa', "(o)");
	char_map.put('\u24ab', "(p)");
	char_map.put('\u24ac', "(q)");
	char_map.put('\u24ad', "(r)");
	char_map.put('\u24ae', "(s)");
	char_map.put('\u24af', "(t)");
	char_map.put('\u24b0', "(u)");
	char_map.put('\u24b1', "(v)");
	char_map.put('\u24b2', "(w)");
	char_map.put('\u24b3', "(x)");
	char_map.put('\u24b4', "(y)");
	char_map.put('\u24b5', "(z)");
	char_map.put('\u24b6', "A");
	char_map.put('\u24b7', "B");
	char_map.put('\u24b8', "C");
	char_map.put('\u24b9', "D");
	char_map.put('\u24ba', "E");
	char_map.put('\u24bb', "F");
	char_map.put('\u24bc', "G");
	char_map.put('\u24bd', "H");
	char_map.put('\u24be', "I");
	char_map.put('\u24bf', "J");
	char_map.put('\u24c0', "K");
	char_map.put('\u24c1', "L");
	char_map.put('\u24c2', "M");
	char_map.put('\u24c3', "N");
	char_map.put('\u24c4', "O");
	char_map.put('\u24c5', "P");
	char_map.put('\u24c6', "Q");
	char_map.put('\u24c7', "R");
	char_map.put('\u24c8', "S");
	char_map.put('\u24c9', "T");
	char_map.put('\u24ca', "U");
	char_map.put('\u24cb', "V");
	char_map.put('\u24cc', "W");
	char_map.put('\u24cd', "X");
	char_map.put('\u24ce', "Y");
	char_map.put('\u24cf', "Z");
	char_map.put('\u24d0', "a");
	char_map.put('\u24d1', "b");
	char_map.put('\u24d2', "c");
	char_map.put('\u24d3', "d");
	char_map.put('\u24d4', "e");
	char_map.put('\u24d5', "f");
	char_map.put('\u24d6', "g");
	char_map.put('\u24d7', "h");
	char_map.put('\u24d8', "i");
	char_map.put('\u24d9', "j");
	char_map.put('\u24da', "k");
	char_map.put('\u24db', "l");
	char_map.put('\u24dc', "m");
	char_map.put('\u24dd', "n");
	char_map.put('\u24de', "o");
	char_map.put('\u24df', "p");
	char_map.put('\u24e0', "q");
	char_map.put('\u24e1', "r");
	char_map.put('\u24e2', "s");
	char_map.put('\u24e3', "t");
	char_map.put('\u24e4', "u");
	char_map.put('\u24e5', "v");
	char_map.put('\u24e6', "w");
	char_map.put('\u24e7', "x");
	char_map.put('\u24e8', "y");
	char_map.put('\u24e9', "z");
	char_map.put('\u24ea', "0");
	char_map.put('\u24eb', "(11)");
	char_map.put('\u24ec', "(12)");
	char_map.put('\u24ed', "(13)");
	char_map.put('\u24ee', "(14)");
	char_map.put('\u24ef', "(15)");
	char_map.put('\u24f0', "(16)");
	char_map.put('\u24f1', "(17)");
	char_map.put('\u24f2', "(18)");
	char_map.put('\u24f3', "(19)");
	char_map.put('\u24f4', "(20)");
	char_map.put('\u24f5', "(1)");
	char_map.put('\u24f6', "(2)");
	char_map.put('\u24f7', "(3)");
	char_map.put('\u24f8', "(4)");
	char_map.put('\u24f9', "(5)");
	char_map.put('\u24fa', "(6)");
	char_map.put('\u24fb', "(7)");
	char_map.put('\u24fc', "(8)");
	char_map.put('\u24fd', "(9)");
	char_map.put('\u24fe', "(10)");
	char_map.put('\u24ff', "(0)");
	char_map.put('\u3000', " ");
	char_map.put('\u3001', ",");
	char_map.put('\u3003', "\"");
	char_map.put('\u3007', "0");
	char_map.put('\u3008', "&lt;");
	char_map.put('\u3009', ">");
	char_map.put('\u300a', "&lt;&lt;");
	char_map.put('\u300b', ">>");
	char_map.put('\u301b', "]");
	char_map.put('\u301c', "~");
	char_map.put('\u301d', "\"");
	char_map.put('\u301e', "\"");
	char_map.put('\u301f', "\"");
	char_map.put('\u3021', "1");
	char_map.put('\u3022', "2");
	char_map.put('\u3023', "3");
	char_map.put('\u3024', "4");
	char_map.put('\u3025', "5");
	char_map.put('\u3026', "6");
	char_map.put('\u3027', "7");
	char_map.put('\u3028', "8");
	char_map.put('\u3029', "9");
	char_map.put('\ufb00', "ff");
	char_map.put('\ufb01', "fi");
	char_map.put('\ufb02', "fl");
	char_map.put('\ufb03', "ffi");
	char_map.put('\ufb04', "ffl");
	char_map.put('\ufb05', "st");
	char_map.put('\ufb06', "st");
	char_map.put('\ufb29', "+");
	char_map.put('\ufe50', ",");
	char_map.put('\ufe51', ",");
	char_map.put('\ufe52', ".");
	char_map.put('\ufe54', ";");
	char_map.put('\ufe55', ":");
	char_map.put('\ufe56', "?");
	char_map.put('\ufe57', "!");
	char_map.put('\ufe58', "-");
	char_map.put('\ufe59', "(");
	char_map.put('\ufe5a', ")");
	char_map.put('\ufe5b', "{");
	char_map.put('\ufe5c', "}");
	char_map.put('\ufe5d', "(");
	char_map.put('\ufe5e', ")");
	char_map.put('\ufe5f', "#");
	char_map.put('\ufe60', "&amp;"); // can't introduce a stray "&" into the text!
	char_map.put('\ufe61', "*");
	char_map.put('\ufe62', "+");
	char_map.put('\ufe63', "-");
	char_map.put('\ufe64', "&lt;");
	char_map.put('\ufe65', ">");
	char_map.put('\ufe66', "=");
	char_map.put('\ufe69', "$");
	char_map.put('\ufe6a', "%");
	char_map.put('\ufe6b', "@");
	char_map.put('\uff01', "!");
	char_map.put('\uff02', "\"");
	char_map.put('\uff03', "#");
	char_map.put('\uff04', "$");
	char_map.put('\uff05', "%");
	char_map.put('\uff06', "&amp;"); // can't introduce a stray "&" into the text!
	char_map.put('\uff07', "'");
	char_map.put('\uff08', "(");
	char_map.put('\uff09', ")");
	char_map.put('\uff0a', "*");
	char_map.put('\uff0b', "+");
	char_map.put('\uff0c', ",");
	char_map.put('\uff0d', "-");
	char_map.put('\uff0e', ".");
	char_map.put('\uff0f', "/");
	char_map.put('\uff10', "0");
	char_map.put('\uff11', "1");
	char_map.put('\uff12', "2");
	char_map.put('\uff13', "3");
	char_map.put('\uff14', "4");
	char_map.put('\uff15', "5");
	char_map.put('\uff16', "6");
	char_map.put('\uff17', "7");
	char_map.put('\uff18', "8");
	char_map.put('\uff19', "9");
	char_map.put('\uff1a', ":");
	char_map.put('\uff1b', ";");
	char_map.put('\uff1c', "&lt;");
	char_map.put('\uff1d', "=");
	char_map.put('\uff1e', ">");
	char_map.put('\uff1f', "?");
	char_map.put('\uff20', "@");
	char_map.put('\uff21', "A");
	char_map.put('\uff22', "B");
	char_map.put('\uff23', "C");
	char_map.put('\uff24', "D");
	char_map.put('\uff25', "E");
	char_map.put('\uff26', "F");
	char_map.put('\uff27', "G");
	char_map.put('\uff28', "H");
	char_map.put('\uff29', "I");
	char_map.put('\uff2a', "J");
	char_map.put('\uff2b', "K");
	char_map.put('\uff2c', "L");
	char_map.put('\uff2d', "M");
	char_map.put('\uff2e', "N");
	char_map.put('\uff2f', "O");
	char_map.put('\uff30', "P");
	char_map.put('\uff31', "Q");
	char_map.put('\uff32', "R");
	char_map.put('\uff33', "S");
	char_map.put('\uff34', "T");
	char_map.put('\uff35', "U");
	char_map.put('\uff36', "V");
	char_map.put('\uff37', "W");
	char_map.put('\uff38', "X");
	char_map.put('\uff39', "Y");
	char_map.put('\uff3a', "Z");
	char_map.put('\uff3b', "[");
	char_map.put('\uff3d', "]");
	char_map.put('\uff3e', "^");
	char_map.put('\uff3f', "_");
	char_map.put('\uff40', "'");
	char_map.put('\uff41', "a");
	char_map.put('\uff42', "b");
	char_map.put('\uff43', "c");
	char_map.put('\uff44', "d");
	char_map.put('\uff45', "e");
	char_map.put('\uff46', "f");
	char_map.put('\uff47', "g");
	char_map.put('\uff48', "h");
	char_map.put('\uff49', "i");
	char_map.put('\uff4a', "j");
	char_map.put('\uff4b', "k");
	char_map.put('\uff4c', "l");
	char_map.put('\uff4d', "m");
	char_map.put('\uff4e', "n");
	char_map.put('\uff4f', "o");
	char_map.put('\uff50', "p");
	char_map.put('\uff51', "q");
	char_map.put('\uff52', "r");
	char_map.put('\uff53', "s");
	char_map.put('\uff54', "t");
	char_map.put('\uff55', "u");
	char_map.put('\uff56', "v");
	char_map.put('\uff57', "w");
	char_map.put('\uff58', "x");
	char_map.put('\uff59', "y");
	char_map.put('\uff5a', "z");
	char_map.put('\uff5b', "{");
	char_map.put('\uff5c', "|");
	char_map.put('\uff5d', "}");
	char_map.put('\uff5e', "~");
	char_map.put('\uff5f', "(");
	char_map.put('\uff60', ")");
	char_map.put('\uff64', ",");
	char_map.put('\uffe9', "&lt;-");
	char_map.put('\uffeb', "->");

	// 08/11/2011
	char_map.put('\u2000', " ");
	char_map.put('\u2001', " ");
	char_map.put('\u2004', " ");
	char_map.put('\u2005', " ");
	char_map.put('\u2006', " ");
	char_map.put('\u2007', " ");
	char_map.put('\u2008', " ");
	char_map.put('\u200A', " ");
	char_map.put('\u200B', " ");
	char_map.put('\u2215', "/");
	char_map.put('\u221e', "infinity");
	char_map.put('\u2248', "~");
	char_map.put('\u207b', "-");
	char_map.put('\u208b', "-");
	char_map.put('\u2212', "-");
	char_map.put('\u223c', "~");
	char_map.put('\u2264', "&lt;=");
	char_map.put('\u2265', ">=");
	char_map.put('\uc2a0', " ");
	char_map.put('\ufeff', " ");

	// From MetamorphoSys

	char_map.put('\u200E', ""); // LEFT-TO-RIGHT MARK
	// char_map.put('\u2020', ""); // dagger
	char_map.put('\u2217', "*");
	// char_map.put('\u0092', "");  // "PRIVATE USE TWO"

	// 05/30/2012

	char_map.put('\u0138', "K"); // Decimal=312 (Latin Small Letter KRA)
	char_map.put('\u0150', "O"); // Decimal=330 (Latin Capital Letter O With Double Acute)
	char_map.put('\u018d', "d"); // Decimal=397 (Latin Small Letter Turned Delta)

	char_map.put('\u018f', "e"); // Decimal=399 (Latin Capital Letter Schwa)
	char_map.put('\u0189', "D"); // Decimal=425 (Latin Capital Letter African D)
	char_map.put('\u0190', "E"); // Decimal=426 (Latin Capital Letter Open E)
	char_map.put('\u01b7', "3"); // Decimal=439 (Latin Capital Letter Ezh)
	char_map.put('\u01b8', "E"); // Decimal=440 (Latin Capital Letter Ezh Reversed)
	char_map.put('\u01b9', "e"); // Decimal=441 (Latin Small Letter Ezh Reversed)
	char_map.put('\u01ba', "e"); // Decimal=442 (Latin Small Letter Ezh With Tail)
	char_map.put('\u01be', "t"); // Decimal=446 (Latin Letter Inverted Glottal Stop With Stroke)
	char_map.put('\u01bf', "p"); // Decimal=447 (Latin Letter Wynn)
	char_map.put('\u01c1', "||"); // Decimal=449 (Latin Letter Lateral Click)
	char_map.put('\u01ee', "3"); // Decimal=494 (Latin Capital Letter Ezh With Caron)
	char_map.put('\u01ef', "3"); // Decimal=495 (Latin Small Letter Ezh With Caron)

	char_map.put('\u0300', ""); //  COMBINING GRAVE ACCENT
	char_map.put('\u0301', ""); //  COMBINING ACUTE ACCENT
	char_map.put('\u0302', ""); //  COMBINING CIRCUMFLEX ACCENT
	char_map.put('\u0303', ""); //  COMBINING TILDE
	char_map.put('\u0304', ""); //  COMBINING MACRON
	char_map.put('\u0305', ""); //  COMBINING OVERLINE
	char_map.put('\u0306', ""); //  COMBINING BREVE
	char_map.put('\u0307', ""); //  COMBINING DOT ABOVE
	char_map.put('\u0308', ""); //  COMBINING DIAERESIS
	char_map.put('\u0309', ""); //  COMBINING HOOK ABOVE
	char_map.put('\u030A', ""); //  COMBINING RING ABOVE
	char_map.put('\u030B', ""); //  COMBINING DOUBLE ACUTE ACCENT
	char_map.put('\u030C', ""); //  COMBINING CARON
	char_map.put('\u030D', ""); //  COMBINING VERTICAL LINE ABOVE
	char_map.put('\u030E', ""); //  COMBINING DOUBLE VERTICAL LINE ABOVE
	char_map.put('\u030F', ""); //  COMBINING DOUBLE GRAVE ACCENT
	char_map.put('\u0310', ""); //  COMBINING CANDRABINDU
	char_map.put('\u0311', ""); //  COMBINING INVERTED BREVE
	char_map.put('\u0312', ""); //  COMBINING TURNED COMMA ABOVE
	char_map.put('\u0313', ""); //  COMBINING COMMA ABOVE
	char_map.put('\u0314', ""); //  COMBINING REVERSED COMMA ABOVE
	char_map.put('\u0315', ""); //  COMBINING COMMA ABOVE RIGHT
	char_map.put('\u0316', ""); //  COMBINING GRAVE ACCENT BELOW
	char_map.put('\u0317', ""); //  COMBINING ACUTE ACCENT BELOW
	char_map.put('\u0318', ""); //  COMBINING LEFT TACK BELOW
	char_map.put('\u0319', ""); //  COMBINING RIGHT TACK BELOW
	char_map.put('\u031A', ""); //  COMBINING LEFT ANGLE ABOVE
	char_map.put('\u031B', ""); //  COMBINING HORN
	char_map.put('\u031C', ""); //  COMBINING LEFT HALF RING BELOW
	char_map.put('\u031D', ""); //  COMBINING UP TACK BELOW
	char_map.put('\u031E', ""); //  COMBINING DOWN TACK BELOW
	char_map.put('\u031F', ""); //  COMBINING PLUS SIGN BELOW
	char_map.put('\u0320', ""); //  COMBINING MINUS SIGN BELOW
	char_map.put('\u0321', ""); //  COMBINING PALATALIZED HOOK BELOW
	char_map.put('\u0322', ""); //  COMBINING RETROFLEX HOOK BELOW
	char_map.put('\u0323', ""); //  COMBINING DOT BELOW
	char_map.put('\u0324', ""); //  COMBINING DIAERESIS BELOW
	char_map.put('\u0325', ""); //  COMBINING RING BELOW
	char_map.put('\u0326', ""); //  COMBINING COMMA BELOW
	char_map.put('\u0327', ""); //  COMBINING CEDILLA
	char_map.put('\u0328', ""); //  COMBINING OGONEK
	char_map.put('\u0329', ""); //  COMBINING VERTICAL LINE BELOW
	char_map.put('\u032A', ""); //  COMBINING BRIDGE BELOW
	char_map.put('\u032B', ""); //  COMBINING INVERTED DOUBLE ARCH BELOW
	char_map.put('\u032C', ""); //  COMBINING CARON BELOW
	char_map.put('\u032D', ""); //  COMBINING CIRCUMFLEX ACCENT BELOW
	char_map.put('\u032E', ""); //  COMBINING BREVE BELOW
	char_map.put('\u032F', ""); //  COMBINING INVERTED BREVE BELOW
	char_map.put('\u0330', ""); //  COMBINING TILDE BELOW
	char_map.put('\u0331', ""); //  COMBINING MACRON BELOW
	char_map.put('\u0332', ""); //  COMBINING LOW LINE
	char_map.put('\u0333', ""); //  COMBINING DOUBLE LOW LINE
	char_map.put('\u0334', ""); //  COMBINING TILDE OVERLAY
	char_map.put('\u0335', ""); //  COMBINING SHORT STROKE OVERLAY
	char_map.put('\u0336', ""); //  COMBINING LONG STROKE OVERLAY
	char_map.put('\u0337', ""); //  COMBINING SHORT SOLIDUS OVERLAY
	char_map.put('\u0338', ""); //  COMBINING LONG SOLIDUS OVERLAY
	char_map.put('\u0339', ""); //  COMBINING RIGHT HALF RING BELOW
	char_map.put('\u033A', ""); //  COMBINING INVERTED BRIDGE BELOW
	char_map.put('\u033B', ""); //  COMBINING SQUARE BELOW
	char_map.put('\u033C', ""); //  COMBINING SEAGULL BELOW
	char_map.put('\u033D', ""); //  COMBINING X ABOVE
	char_map.put('\u033E', ""); //  COMBINING VERTICAL TILDE
	char_map.put('\u033F', ""); //  COMBINING DOUBLE OVERLINE
	char_map.put('\u0340', ""); //  COMBINING GRAVE TONE MARK
	char_map.put('\u0341', ""); //  COMBINING ACUTE TONE MARK
	char_map.put('\u0342', ""); //  COMBINING GREEK PERISPOMENI
	char_map.put('\u0343', ""); //  COMBINING GREEK KORONIS
	char_map.put('\u0344', ""); //  COMBINING GREEK DIALYTIKA TONOS
	char_map.put('\u0345', ""); //  COMBINING GREEK YPOGEGRAMMENI
	char_map.put('\u0346', ""); //  COMBINING BRIDGE ABOVE
	char_map.put('\u0347', ""); //  COMBINING EQUALS SIGN BELOW
	char_map.put('\u0348', ""); //  COMBINING DOUBLE VERTICAL LINE BELOW
	char_map.put('\u0349', ""); //  COMBINING LEFT ANGLE BELOW
	char_map.put('\u034A', ""); //  COMBINING NOT TILDE ABOVE
	char_map.put('\u034B', ""); //  COMBINING HOMOTHETIC ABOVE
	char_map.put('\u034C', ""); //  COMBINING ALMOST EQUAL TO ABOVE
	char_map.put('\u034D', ""); //  COMBINING LEFT RIGHT ARROW BELOW
	char_map.put('\u034E', ""); //  COMBINING UPWARDS ARROW BELOW
	char_map.put('\u0360', ""); //  COMBINING DOUBLE TILDE
	char_map.put('\u0361', ""); //  COMBINING DOUBLE INVERTED BREVE
	char_map.put('\u0362', ""); //  COMBINING DOUBLE RIGHTWARDS ARROW BELOW
	char_map.put('\u0406', "I");
	char_map.put('\u0407', "I");
	char_map.put('\u0408', "J");
	char_map.put('\u0410', "A"); // FML 2016/06/08; http://www.fileformat.info/info/unicode/char/0410/index.htm
	char_map.put('\u0411', "B");
	char_map.put('\u0412', "V");
	char_map.put('\u0413', "G");
	char_map.put('\u0414', "D");
	char_map.put('\u0415', "E");
	char_map.put('\u0416', "ZH");
	char_map.put('\u0417', "ZE");
	char_map.put('\u0418', "I");
	char_map.put('\u0419', "I");
	char_map.put('\u041a', "K");
	char_map.put('\u041b', "L");
	char_map.put('\u041c', "M");
	char_map.put('\u041d', "N");
	char_map.put('\u041e', "O");
	char_map.put('\u041f', "P");
	char_map.put('\u0420', "R");
	char_map.put('\u0421', "S");
	char_map.put('\u0422', "T");
	char_map.put('\u0423', "U");
	char_map.put('\u0424', "F");
	char_map.put('\u0425', "X");
	char_map.put('\u0426', "TS");
	char_map.put('\u0427', "CH");
	char_map.put('\u0428', "SH");
	char_map.put('\u0429', "SHCH");
	// char_map.put('\u042a', "");  // "CYRILLIC CAPITAL LETTER HARD SIGN"
	char_map.put('\u042b', "I");
	// char_map.put('\u042c', "");     // "CYRILLIC CAPITAL LETTER SOFT SIGN"
	char_map.put('\u042d', "E");
	char_map.put('\u042e', "YU");
	char_map.put('\u042f', "YA");
	char_map.put('\u0430', "a");
	char_map.put('\u0431', "b");
	char_map.put('\u0432', "v");
	char_map.put('\u0433', "g");
	char_map.put('\u0434', "d");
	char_map.put('\u0435', "e");
	char_map.put('\u0436', "zh");
	char_map.put('\u0437', "ze");
	char_map.put('\u0438', "i");
	char_map.put('\u0439', "i");
	char_map.put('\u043a', "k");
	char_map.put('\u043b', "l");
	char_map.put('\u043c', "m");
	char_map.put('\u043d', "n");
	char_map.put('\u043e', "o");
	char_map.put('\u043f', "p");
	char_map.put('\u0440', "r");
	char_map.put('\u0441', "s");
	char_map.put('\u0442', "t");
	char_map.put('\u0443', "u");
	char_map.put('\u0444', "f");
	char_map.put('\u0445', "x");
	char_map.put('\u0446', "ts");
	char_map.put('\u0447', "ch");
	char_map.put('\u0448', "sh");
	char_map.put('\u0449', "shch");
	// char_map.put('\u044a', "");   // "CYRILLIC SMALL LETTER HARD SIGN"
	char_map.put('\u044b', "i");
	// char_map.put('\u044c', "");   // "CYRILLIC SMALL LETTER SOFT SIGN"
	char_map.put('\u044d', "e");
	char_map.put('\u044e', "yu");
	char_map.put('\u044f', "ya");
	char_map.put('\u0450', "e");
	char_map.put('\u0451', "e");
	char_map.put('\u0452', "d");
	char_map.put('\u0453', "g");
	char_map.put('\u0454', "e");
	char_map.put('\u0455', "s");
	char_map.put('\u0456', "i");
	char_map.put('\u0457', "i");
	char_map.put('\u0458', "j");
	char_map.put('\u0459', "l");
	char_map.put('\u045a', "nj");
	char_map.put('\u045b', "tsh");
	char_map.put('\u045c', "k");
	char_map.put('\u045d', "i");
	char_map.put('\u045e', "u");
	char_map.put('\u045f', "dzh");
	char_map.put('\u0460', "O");
	char_map.put('\u0461', "o");
	char_map.put('\u0462', "YAT");
	char_map.put('\u0463', "yat");
	char_map.put('\u047a', "O");
	char_map.put('\u047b', "o");
	char_map.put('\u04aa', "C");
	char_map.put('\u04ab', "c");
	char_map.put('\u04d1', "a");
	char_map.put('\u04d3', "a");
	char_map.put('\u04d5', "ae");
	char_map.put('\u04e6', "O");
	char_map.put('\u04e7', "o");
	char_map.put('\u05f3', "'");
	char_map.put('\u0627', "|");
	char_map.put('\u200b', ""); // "ZERO-WIDTH SPACE"
	char_map.put('\u200e', ""); // "LEFT-TO-RIGHT MARK"
	char_map.put('\u2022', "*"); // "BULLET"
	char_map.put('\u2261', "="); // "IDENTICAL TO"

	// char_map.put('\u8166', ""); // "Unicode Han Character 'brain'"
	// char_map.put('\u982d', ""); // "Unicode Han Character 'head; top; chief, first; boss"
	// char_map.put('\ub1cc', "");  // Invalid Unicode character
	// char_map.put('\ub450', "");  // Invalid Unicode character
	// char_map.put('\uf030', "");  // Invalid Unicode character
    }

    // Replaces special characters with look-like ASCII chars

    protected static StringBuilder replUTF8(StringBuilder input) throws IOException {
	// PrintStream ps = new PrintStream(System.out, true, "UTF-8");
	final String UNMAPPED = "?";
	StringBuilder tempBuf = new StringBuilder();
	// https://stackoverflow.com/questions/10933620/display-special-characters-using-system-out-println
	int len = input.length();
	String rep_char = "";

	//boolean DEBUG = true;
	//   if ( DEBUG ) {
	//     System.out.println("STRING:" + input);
	//   } // if

	for (int i = 0; i < len; i++) {

	    rep_char = char_map.get(input.charAt(i));
	    //      if ( DEBUG ) {
	    //      	String s0 = i + ":" + input.charAt(i);
	    //      	String s1 = "CHAR:" + s0;
	    //      	System.out.println(s1);
	    //      	ps.println(s1);
	    //      	System.out.println(s1 + ":" + rep_char);
	    //      	System.out.println(( rep_char == null ) ? s1 + ":NO\n" : s1 + ":YES\n");
	    //            } // if

	    if (rep_char == null) {
		tempBuf.append(UNMAPPED);
	    } // if
	    else {
		tempBuf.append(rep_char);
	    } // else
	} // for
	  //    ps.close();

	return tempBuf;
    } // replUTF8

    protected static String ReplaceLooklike(String input) throws IOException {
	// PrintStream ps = new PrintStream(System.out, true, "UTF-8");
	final String UNMAPPED = "?";
	StringBuilder tempBuf = new StringBuilder();
	// https://stackoverflow.com/questions/10933620/display-special-characters-using-system-out-println
	int len = input.length();
	String rep_char = "";

	//boolean DEBUG = true;
	//   if ( DEBUG ) {
	//     System.out.println("STRING:" + input);
	//   } // if

	for (int i = 0; i < len; i++) {

	    rep_char = char_map.get(input.charAt(i));
	    //      if ( DEBUG ) {
	    //      	String s0 = i + ":" + input.charAt(i);
	    //      	String s1 = "CHAR:" + s0;
	    //      	System.out.println(s1);
	    //      	ps.println(s1);
	    //      	System.out.println(s1 + ":" + rep_char);
	    //      	System.out.println(( rep_char == null ) ? s1 + ":NO\n" : s1 + ":YES\n");
	    //            } // if

	    if (rep_char == null) {
		tempBuf.append(UNMAPPED);
	    } // if
	    else {
		tempBuf.append(rep_char);
	    } // else
	} // for
	  //    ps.close();

	return tempBuf.toString();
    } // replUTF8

    public static void main(String args[]) {
	try {
	    StringBuffer buf = new StringBuffer();
	    BufferedReader reader;
	    if (args.length > 0)
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
	    else
		reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

	    String Line;
	    while ((Line = reader.readLine()) != null) {
		if (Line.length() > 0)
		    System.out.println(replUTF8(new StringBuilder(Line)));

		else
		    System.out.println("");
	    } // while !done

	    reader.close();
	} // try
	catch (Exception e) {
	    System.err.println(args[0] + "Exception Occurred");
	    System.err.println(e);
	} // catch
    } // main

} // class replace_UTF8
