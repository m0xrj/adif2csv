/* 
ADIF2CSV - Converts a file of Amateur Data Interchange Format (ADIF) data to 
comma separated value (CSV) format with headings
Copyright (C) 2023 Rob Jones (M0XRJ)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.m0xrj.adif;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

class Adif2csv {
    private static final String ADIF_SRC_WSJTX="WSJTX";
    private static final String ADIF_SRC_PSK="PSK";
    private static final String PSKSEQ="QSO_DATE,TIME_ON,OPERATOR,CALL,DISTANCE,GRIDSQUARE,MY_GRIDSQUARE,MODE,SWL,FREQ,APP_PSKREP_BRG,APP_PSKREP_SNR,QSO_COMPLETE";
    private static final String WSJTXSEQ="call,gridsquare,mode,rst_sent,rst_rcvd,qso_date,time_on,qso_date_off,time_off,band,freq,station_callsign,my_gridsquare,tx_pwr,comment,name,operator";
    private static final String CLI_OPT_FILE_SHORT="f";
    private static final String CLI_OPT_FILE_LONG="file";
    private static final String CLI_OPT_FILE_DESCR="Filename of ADIF data to process";
    private static final String CLI_OPT_SRC_SHORT="s";
    private static final String CLI_OPT_SRC_LONG="source";
    private static final String CLI_OPT_SRC_DESCR="Source of ADIF data PSK|WSJTX (default is WSJTX)";
    private static final String CLI_OPT_HELP_SHORT="h";
    private static final String CLI_OPT_HELP_LONG="help";
    private static final String CLI_OPT_HELP_DESCR="Show help information";

    public static void main(String[] args)  {
        if (args.length==1) {
            String[] reArgs=args[0].split(" ");
            if (reArgs.length>1) {
                args=reArgs;
            }
        }
        String adifSource=ADIF_SRC_WSJTX;
        String adifFilename=null;
        String adifFieldSpec=WSJTXSEQ;
        Options cliOptions=getCLIOptions();

        if (args.length>0) {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd=null;
            try {
                cmd = parser.parse(cliOptions, args);
            } catch (ParseException pe) {
                if (pe instanceof UnrecognizedOptionException) {
                    System.err.printf("adif2csv parameter error: %s%n",pe.getMessage());
                } else {
                    System.err.printf("adif2csv error: %s%n",pe.getMessage());
                }
                System.exit(-1);
            }
            if (cmd.hasOption(CLI_OPT_FILE_SHORT)) {
                adifFilename=cmd.getOptionValue("f").trim();
            } else {
                adifFilename="test.adif";
            }
            if (cmd.hasOption(CLI_OPT_HELP_SHORT)) {
                HelpFormatter helpSummary = new HelpFormatter();
                helpSummary.printHelp("adif2csv", cliOptions);
                System.exit(0);
            }
            if (cmd.hasOption(CLI_OPT_SRC_SHORT)) {
                String srcOption=cmd.getOptionValue(CLI_OPT_SRC_SHORT);
                switch(srcOption.trim().toUpperCase()) {
                    case ADIF_SRC_WSJTX -> {
                        adifFieldSpec=WSJTXSEQ;
                        adifSource=ADIF_SRC_WSJTX;
                    }
                    case ADIF_SRC_PSK -> {
                        adifFieldSpec=PSKSEQ;
                        adifSource=ADIF_SRC_PSK;
                    }
                    default -> {
                        System.err.printf("Unrecognised source: %s. Assuming WSJTX ADIF format%n",srcOption);
                    }
                }
            }
        }

        String[] fieldOrder=adifFieldSpec.split(",");
        List<String> orderedKeys = Arrays.asList(fieldOrder);

        File adifFile = new File(adifFilename);
        System.err.printf("Processing %s ADIF file:%s%n",adifSource,adifFile.getAbsolutePath());

        Map<String, String> adifMap = new HashMap<>();
        boolean writeCSVHeader = true;

        if (adifFile.exists()) {
            try (FileReader fr = new FileReader(adifFile)) // reads the file
            {
                BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
                int recNo = 0;
                if (WSJTXSEQ.equals(adifFieldSpec) && !checkWSJTXHeader(br)) {
                    System.err.println("ADIF header not found! Exiting...\n\n");
                    System.err.flush();
                    return;
                }
                if (PSKSEQ.equals(adifFieldSpec) && !checkPSKHeader(br)) {
                    System.err.println("PSK header not found! Exiting...\n\n");
                    System.err.flush();
                    return;
                }
                String line=null;
                while ((line = br.readLine()) != null) {
                    ++recNo;
                    String[] adifFields = line.split("[<>]");
                    for (int i = 1; i < adifFields.length; i += 2) {
                        if (i + 1 < adifFields.length) {
                            String rawKey = adifFields[i];
                            String key = rawKey;
                            if (rawKey.contains(":")) {
                                key = rawKey.split(":")[0];
                            }
                            String value = adifFields[i + 1];
                            adifMap.put(key, value);
                        }
                    }

                    if (writeCSVHeader) {
                        System.out.printf("%s%n", adifFieldSpec);
                        writeCSVHeader=false;
                    }

                    String adifCSV = toCSV(adifMap,orderedKeys);
                    System.out.printf("%s%n", adifCSV);

                }
                System.err.printf("ADIF records processed=%d%n", recNo);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.err.println("File not found!");
        }
    }

    private static String toCSV(Map<String, String> hm,List<String> order) {
        StringBuilder csv = new StringBuilder();
        String key = null;

        Iterator<String> iKeys = order.iterator();

        // If there is at least one key in the data map, 
        if (iKeys.hasNext()) {
            key = iKeys.next();
            csv.append(hm.get(key));
        }
        while (iKeys.hasNext()) {
            csv.append(",");
            key = iKeys.next();
            csv.append(hm.get(key));
        }
        return csv.toString();
    }

    protected static boolean checkWSJTXHeader(BufferedReader br) throws IOException {
        String line = null;
        boolean foundEOH = false;
        boolean foundADIFvers = false;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("<adif_ver:5>")) {
                foundADIFvers=true;
            }
            if (line.startsWith("<eoh>")) {
                foundEOH=true;
                break;
            }
        }

        return foundEOH && foundADIFvers;
    }

    protected static boolean checkPSKHeader(BufferedReader br) throws IOException {
        String line = null;
        boolean test = false;
        if ((line = br.readLine()) != null) {
            if (line.indexOf("PSKReporter.info") > 0) {
                test = true;
            } else {
                System.err.printf("BAD PSKreporter header: %s%n", line);
            }
        }
        return test;
    }

    private static Options getCLIOptions() {
        Options cliOptions=new Options();
        cliOptions.addOption(CLI_OPT_FILE_SHORT, CLI_OPT_FILE_LONG, true, CLI_OPT_FILE_DESCR);
        cliOptions.addOption(CLI_OPT_SRC_SHORT, CLI_OPT_SRC_LONG, true, CLI_OPT_SRC_DESCR);
        cliOptions.addOption(CLI_OPT_HELP_SHORT, CLI_OPT_HELP_LONG, false, CLI_OPT_HELP_DESCR);
        return cliOptions;
    }
}