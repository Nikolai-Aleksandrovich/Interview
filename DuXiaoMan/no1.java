package com.company;

import java.util.Scanner;

public class Main3 {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        String s = scan.nextLine();
        int resultCat = 0;
        int resultPig = 0;
        if (s.length()==5){
            System.out.println("horse");
        }else {
            for (int i = 0; i < 3; i++) {
                if (s.charAt(i)=='c'||s.charAt(i)=='a'||s.charAt(i)=='t'){
                    resultCat++;
                    if (resultCat>=2){
                        System.out.println("cat");
                        break;
                    }
                }
                if (s.charAt(i)=='p'||s.charAt(i)=='i'||s.charAt(i)=='g'){
                    resultPig++;
                    if (resultPig>=2){
                        System.out.println("pig");
                    }
                }
            }
        }

    }
}
