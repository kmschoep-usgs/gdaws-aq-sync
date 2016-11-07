package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Interpolation {

    public static double calculateLinear(LocalDateTime t1, LocalDateTime t2, double x1, double x2, LocalDateTime tInterpolated)
    {
        /*
         * Derivation....
         * Given that
         *  t1 and t2 are the first and second times of two known measurements of "x"
         *  x1 and x2 are the measurements of "x" knnown at times t1 and t2, respectively
         *  tInterp is a time between these two points that interpolation is desired
         *  
         *  slope = rise / run
         *  slope = (x2-x1)/(t2-t1)
         *  
         * Just by staring at things and doing some Excel testing (sorry, a bit of a weak rationale):
         *  xInterp = x1 + (tInterp - t1) * m
         *  xInterp = x1 + (tInterp - t1) * (x2 - x1) / (t2 - t1)
         *  
         * which we observe is idential to the approach taken in David Topping's FORTRAN, which is:
         *  xInterp = ((tInterp - t1)/(t2 - t1)) * (x2 - x1) + x1
         *  
         */
    	
    	//do work in UTC
    	ZoneId utc = ZoneId.of("UTC");
    	Long secondsTime1 = t1.atZone(utc).toEpochSecond();
    	Long secondsTime2 = t2.atZone(utc).toEpochSecond();
    	Long secondsTargetTime = tInterpolated.atZone(utc).toEpochSecond();

        return x1 + (secondsTargetTime - secondsTime1) * (x2 - x1) / (secondsTime2 - secondsTime1);
    }
}
