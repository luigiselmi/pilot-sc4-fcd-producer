package eu.bde.pilot.sc4.fcd;

import java.io.IOException;
import java.util.Arrays;

/**
 * Pick whether we want to run the producer or consumer or MapMatch. This lets us
 * have a single executable as a build target.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Must have either 'producer' or 'consumer' or 'mapmatch' as first argument. \n");
        }
        
        try {
		    switch (args[0]) {
		        case "producer":
		            FlinkFcdProducer.main(Arrays.copyOfRange(args, 1, args.length));
		            break;
		        case "consumer":
		            FlinkFcdConsumer.main(Arrays.copyOfRange(args, 1, args.length));
		            break;
		        case "mapmatch":
                FlinkMapMatch.main(Arrays.copyOfRange(args, 1, args.length));
                break;
		        default:
		            throw new IllegalArgumentException("Don't know how to handle " + args[0]);
		    }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }
}
