# adif2csv
A Java console application that converts a file of Amateur Data Interchange Format (ADIF) data to comma separated value (CSV) format, with headings suitable for import to a spreadsheet. 

Supported ADIF data sources include [WSJT-X](https://wsjt.sourceforge.io/wsjtx.html) and [PSKreporter.info](https://pskreporter.info).

## Invocation 

Mac/Linux terminal:

`./adif2csv <options>`

Windows command:

`adif2csv.bat <options>`

## Command line options

The `adif2csv` command line options are:
```
 -f,--file <arg>     Filename of ADIF data to process
 -h,--help           Show help information
 -s,--source <arg>   Source of ADIF data PSK|WSJTX (default is WSJTX)
```

The ADIF file is assumed to be in the current working directory unless a fully qualified path is provided. 

The CSV output is written to _stdout_ and user messages written to _stderr_, allowing the CSV output to be redirected to a file.

Example (Linux/Mac):

```
bin> ./adif2csv -f WSJTX.adif > WSJTX.csv

Processing WSJTX ADIF file:/tmp/adif2csv/bin/WSJTX.adif.txt
ADIF records processed=3
```
The CSV output is writtten to `WSJTX.csv` in the current working directory.
