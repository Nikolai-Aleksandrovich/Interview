package com.company;


import java.util.*;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        Scanner in = new Scanner(System.in);
        int rowCount = in.nextInt();
        int colCount = in.nextInt();
        int sortCount = in.nextInt();
        int[][] frame = new int[rowCount][colCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                frame[i][j] = in.nextInt();
            }
        }
        for (int o = 0; o < sortCount; o++) {
            int startRow = in.nextInt()-1;
            int endRow = in.nextInt()-1;
            int startCol = in.nextInt()-1;
            int endCol = in.nextInt()-1;
            int sortByCol = in.nextInt()-1;
            int isDesc = in.nextInt();

            int[] base = new int[endRow - startRow + 1];
            int[] sortBase = new int[endRow - startRow + 1];
            for (int j = startRow; j < endRow + 1; j++) {
                base[j - startRow] = frame[j][sortByCol];
                sortBase[j - startRow] = frame[j][sortByCol];
            }
            Arrays.sort(sortBase);

            int[][] moreSpace = new int[endRow - startRow + 1][endCol - startCol + 1];
            if (isDesc == 1) {

                for (int i = base.length-1; i >= 0; i--) {
                    int cur = sortBase[i];
                    for (int j = 0; j < base.length; j++) {
                        if (base[j] == cur) {
                            base[j] = -1;
                            for (int k = 0; k < endCol - startCol + 1; k++) {
                                moreSpace[endCol-startCol-i][k] = frame[j + startRow][k+startCol];
                            }
                            break;
                        }
                    }
                }
                for (int i = 0; i < moreSpace.length; i++) {
                    for (int j = 0; j < moreSpace[0].length; j++) {
                        frame[i + startRow][j + startCol] = moreSpace[i][j];
                    }
                }



            } else {
                for (int i = 0 ; i < base.length ; i++) {
                    int cur = sortBase[i];
                    for (int j = 0; j < base.length; j++) {
                        if (base[j] == cur) {
                            base[j] = -1;
                            for (int k = 0; k < endCol - startCol + 1; k++) {
                                moreSpace[i][k] = frame[j + startRow][k+startCol];
                            }
                            break;
                        }
                    }
                }
                for (int i = 0; i < moreSpace.length; i++) {
                    for (int j = 0; j < moreSpace[0].length; j++) {
                        frame[i + startRow][j + startCol] = moreSpace[i][j];
                    }
                }
            }

        }

        for (int[] ele: frame
        ) {
            for (int i = 0; i < ele.length-1; i++) {
                System.out.print(ele[i]+" ");
            }
            System.out.println(ele[ele.length-1]);


        }

    }
}