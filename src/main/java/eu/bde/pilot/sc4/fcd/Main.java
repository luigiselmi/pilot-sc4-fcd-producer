package eu.bde.pilot.sc4.fcd;

import java.io.IOException;

/**
 * Pick whether we want to run as producer or consumer. This lets us
 * have a single executable as a build target.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Must have either 'producer' or 'consumer' as first argument \n"
                + "and the path to the gzipped file of GPS records as 2nd argument for a producer. \n");
        }
        
        try {
		    switch (args[0]) {
		        case "producer":
		            FcdProducer.main(args);
		            break;
		        case "consumer":
		            FcdConsumer.main(args);
		            break;
		        default:
		            throw new IllegalArgumentException("Don't know how to do " + args[0]);
		    }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }
}
