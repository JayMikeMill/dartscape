package com.gworks.dartscape.logic;

import com.google.common.primitives.Ints;
import com.gworks.dartscape.data.GameData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class X01Logic {
    private static final int[] NUMBERS = {
            25, 20, 19, 18, 17, 16, 15, 14, 13, 12,
            11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    };


    private static final int[] POSSIBLE_SCORES = {
            60, 57, 54, 51, 50, 48, 45, 42, 40, 39, 38, 36, 34, 33, 32, 30,
            28, 27, 26, 25, 24, 22, 21,
            20, 19, 18, 17, 16, 15, 14, 13, 12, 11,
            10, 9, 8, 7, 6, 5, 4, 3, 2, 1
    };

    private static final int[][] POSSIBLE_OUTS = {
            // single outs
            {25, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11,
                    10, 9, 8, 7, 6, 5, 4, 3, 2, 1 },
            // double outs
            {50, 40, 38, 36, 34, 32, 30, 28, 26, 24, 22,
                    20, 18, 16, 14, 12, 10, 8, 6, 4, 2 },
            // triple outs
            {60, 57, 54, 51, 48, 45, 42, 39, 36, 33, 30,
                    27, 24, 21, 18, 15, 12, 9, 6, 3 }
    };

    // 0 = number, 1 = multi
    public static int[] x01GetBestOut(GameData data, int score) {
        boolean[] outs = data.x01GetInsOuts(false);

        int maxOut = outs[2] ? 180 : outs[1] ? 140 : outs[0] ? 50 : 0;

        if(score >= maxOut) return new int[]{20, 3};
        if(outs[3] && score == 25) return new int[] { 25, 1 };

        for(int i = 0; i < 3; i++) {
            if (outs[i]) {
                if (Ints.contains(POSSIBLE_OUTS[i],  score))
                    return new int[]{score / (i + 1), i + 1};
            }
        }

        for(int i = 0; i < 3; i++) {
            if (outs[i]) {
                // <= 120

                for (int pScore : POSSIBLE_SCORES) {
                    if(Ints.contains(POSSIBLE_OUTS[i], score - pScore)) {
                        int multi = getBiggestMulti(pScore);
                        return new int[]{ pScore / multi, multi };
                    }
                }

                // <= 180
                for (int pScore1 : POSSIBLE_SCORES) {
                    for (int pScore2 : POSSIBLE_SCORES) {
                        int tempScore = score - (pScore1 + pScore2);
                        if (Ints.contains(POSSIBLE_OUTS[i], tempScore)) {
                            int tscore = Math.max(pScore1, pScore2);
                            int multi = getBiggestMulti(tscore);
                            return new int[]{tscore / multi, multi};
                        }
                    }
                }
            }
        }

        return  new int[] { 20, 3};
    }

    private static int getBiggestMulti(int score) {
        if(score <= 20 || score == 25) return 1;
        if(score % 2 == 0 && (score <= 40 || score == 50)) return 2;
        if(score % 3 == 0) return 3;

        return 0;
    }

}