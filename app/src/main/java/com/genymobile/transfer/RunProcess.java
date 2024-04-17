package com.genymobile.transfer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RunProcess {
    public static String runProcess(String terminal) throws Exception{
        Process process = Runtime.getRuntime().exec(terminal);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        reader.close();
        process.getOutputStream().close();
        process.getErrorStream().close();
        return output.toString();
    }
    public static void runProcessAsync(String terminal) throws Exception {
        Process process = Runtime.getRuntime().exec(terminal);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
