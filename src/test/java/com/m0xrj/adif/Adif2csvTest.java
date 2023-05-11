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

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Adif2csvTest {
    private Path resourceDirectory = Paths.get("src", "test", "resources");
    private String absolutePath = resourceDirectory.toFile().getAbsolutePath();
    private ClassLoader classLoader = getClass().getClassLoader();

    @Test
    public void testWSJTXadif() {
        System.out.println("Adif2csvTest.testAdif2csv:Working Directory = " + System.getProperty("user.dir"));
        System.out.printf("Path of test resources is %s%n", absolutePath);

        String testData1 = "WSJTX.adif.txt";
        System.out.printf("Test data filename: %s%n",testData1);
        File testFile1 = new File(classLoader.getResource(testData1).getFile());
        String absolutePath1 = testFile1.getAbsolutePath();
        BufferedReader br1 = getReader(absolutePath1);

        try {
            assertTrue("adif2csv should loading a WSJTX file", Adif2csv.checkWSJTXHeader(br1));
            System.out.println("testWSJTXadif() passed");
        } catch (IOException ioe) {
            fail("IOException!");
        }
    }

    @Test
    public void testPSKadif() {
        String testData2 = "PSK.adif.txt";
        System.out.printf("Test data filename: %s%n",testData2);
        File testFile2 = new File(classLoader.getResource(testData2).getFile());
        String absolutePath2 = testFile2.getAbsolutePath();
        BufferedReader br2 = getReader(absolutePath2);
        
        try {
            assertTrue("adif2csv should be loading a PSK file", Adif2csv.checkPSKHeader(br2));
            System.out.println("testPSKadif() passed");
        } catch (IOException ioe) {
            fail("IOException!");
        }
    }

    private BufferedReader getReader(String absolutePath) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(absolutePath); // reads the file
            br = new BufferedReader(fr); // creates a buffering character input stream
        } catch (FileNotFoundException fnfe) {
            fail("File not found exception!");
        }
        return br;
    }
}
