package com.company;

import java.util.Scanner;

public class Main3 {

    public static void main(String[] args) {
        long[] cheems = new long[100004];
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        cheems[0] = 1;
        cheems[1] = 1;
        cheems[2] = 1;
        cheems[3] = 2;
        for (int i = 4; i <= n; i++) {
            cheems[i] = cheems[i-1]+cheems[i-3];
            cheems[i]%=998244353;

        }
        System.out.println(cheems[n]);
    }
}
