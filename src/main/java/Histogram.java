import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

/* single color intensity */
class Color implements WritableComparable<Color> {

    public short type;       /* red=1, green=2, blue=3 */
    public short intensity;  /* between 0 and 255 */

    //Init Method
    Color() {

    }

    /* need class constructors, toString, write, readFields, and compareTo methods */

    //Color class constructor
    Color(short t, short i) {

        type = t;
        intensity = i;
    }

    //toString method for returning output
    @Override
    public String toString() {

        return this.type + " " + this.intensity;
    }

    //Writing data to the text file
    @Override
    public void write(DataOutput dataOutput) throws IOException {

        dataOutput.writeShort(type);
        dataOutput.writeShort(intensity);
    }

    //Reading data from the file
    @Override
    public void readFields(DataInput dataInput) throws IOException {

        type = dataInput.readShort();
        intensity = dataInput.readShort();
    }

    //Comparing each color type and intensity
    @Override
    public int compareTo(Color color) {

        //If color type is similar, then check for intensity
        if (this.type == color.type) {

            if (this.intensity > color.intensity) {

                return 1;
            }
            else if (this.intensity == color.intensity) {

                return 0;
            }
            else {

                return -1;
            }
        }
        else {

            if (this.type > color.type) {

                return 1;
            }
            else {

                return -1;
            }
        }
    }

    //Equals method, auto-generated by IntelliJ IDE.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Color color = (Color) o;
        return type == color.type &&
                intensity == color.intensity;
    }

    //Hashing the value of the object using a random integer.
    @Override
    public int hashCode() {

        return (this.type * 1000 + this.intensity);
    }
}

public class Histogram {

    public static class HistogramMapper extends Mapper<Object, Text, Color, IntWritable> {
        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            /* write your mapper code */

            //Reading data from the file
            Scanner s = new Scanner(value.toString()).useDelimiter(",");
            Color red = new Color((short) 1, s.nextShort());
            Color green = new Color((short) 2, s.nextShort());
            Color blue = new Color((short) 3, s.nextShort());

            context.write(red, new IntWritable(1));
            context.write(green, new IntWritable(1));
            context.write(blue, new IntWritable(1));

            //Closing the scanner instance.
            s.close();
        }
    }

    //HistogramInMapper class for job 2. An in-mapper class.
    public static class HistogramInMapper extends Mapper<Object, Text, Color, IntWritable> {

        //Declaring instance variables.
        public Hashtable<Color, Integer> hashtable;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {

            //Intializing our hashtable using the instance variable created above.
            hashtable = new Hashtable<Color, Integer>();
        }

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            /* write your mapper code */

            //Reading data from the file, same code as the mapper class.
            Scanner s = new Scanner(value.toString()).useDelimiter(",");
            Color red = new Color((short) 1, s.nextShort());
            Color green = new Color((short) 2, s.nextShort());
            Color blue = new Color((short) 3, s.nextShort());

            //Using color type as the key and putting it into the hash table, and incrementing the value if hashtable contains the key.
            if(hashtable.containsKey(red)) {

                hashtable.put(red, hashtable.get(red) + 1);
            }
            else {

                hashtable.put(red, 1);
            }

            if(hashtable.containsKey(green)) {

                hashtable.put(green, hashtable.get(green) + 1);
            }
            else {

                hashtable.put(green, 1);
            }

            if(hashtable.containsKey(blue)) {

                hashtable.put(blue, hashtable.get(blue) + 1);
            }
            else {

                hashtable.put(blue, 1);
            }

            //Closing the scanner instance.
            s.close();
        }

        //Cleanup method for clearing out hashtable after inmapper passes values to the reducer.
        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

            for(Color color : hashtable.keySet()) {

                context.write(color, new IntWritable(hashtable.get(color)));
            }

            hashtable.clear();
        }
    }

    //HistogramCombiner class, same code as the reducer, writes sum as with a new IntWritable object.
    public static class HistogramCombiner extends Reducer<Color, IntWritable, Color, IntWritable> {
        @Override
        public void reduce(Color key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            /* write your reducer code */

            //Initializing sum to zero
            int sum = 0;

            //Incrementing sum at each occurence of a particular color type and intensity
            for (IntWritable v : values) {

                sum += v.get();
            }

            context.write(key, new IntWritable(sum));
        }
    }

    public static class HistogramReducer extends Reducer<Color, IntWritable, Color, LongWritable> {
        @Override
        public void reduce(Color key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            /* write your reducer code */

            //Initializing sum to zero
            int sum = 0;

            //Incrementing sum at each occurence of a particular color type and intensity
            for (IntWritable v : values) {

                sum += v.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {

        //Driver code for both of the jobs.
        Job job2 = Job.getInstance();
        job2.setJobName("Histogram_InMapper1");
        job2.setJarByClass(Histogram.class);
        job2.setOutputKeyClass(Color.class);
        job2.setOutputValueClass(IntWritable.class);
        job2.setMapOutputKeyClass(Color.class);
        job2.setMapOutputValueClass(IntWritable.class);
        job2.setMapperClass(HistogramMapper.class);
        job2.setReducerClass(HistogramReducer.class);
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);
        job2.setCombinerClass(HistogramCombiner.class);
        FileInputFormat.setInputPaths(job2, new Path(args[0]));
        FileOutputFormat.setOutputPath(job2, new Path(args[1]));
        job2.waitForCompletion(true);

        Job job1 = Job.getInstance();
        job1.setJobName("Histogram_InMapper");
        job1.setJarByClass(Histogram.class);
        job1.setOutputKeyClass(Color.class);
        job1.setOutputValueClass(IntWritable.class);
        job1.setMapOutputKeyClass(Color.class);
        job1.setMapOutputValueClass(IntWritable.class);
        job1.setMapperClass(HistogramInMapper.class);
        job1.setReducerClass(HistogramReducer.class);
        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.setInputPaths(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path(args[1] + "2"));
        job1.waitForCompletion(true);
    }
}