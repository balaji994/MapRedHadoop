# MapRedHadoop

This Repository got two class mainly WordCount and the SentimentalAnalysis(simple one). 
   #WordCount
      It takes the text file as an input and counts all the words individually in it and prints to an output folder with Unix executable 
      file named as "part-r-0000". It needs Hadoop 1.2.1 Single node Cluster  

# Requirements To Run:
     1. Java 1.8,
     2. Hadoop 1.2.1 (Single Node Cluster)
     3. Flume 1.7 (For SentimentalAnalysis not for WordCount) Used to get Twitter data on HDFS
I have also uploaded the hadoop and flume repositories  
#Word Count
Word Count is a simple and easy to understand algorithm which can be implemented as a mapreduce application easily. Given a set of text documents the program counts the number of occurrences of each word. The algorithm consists of three main sections.

1.	Main Program
2.	Mapper
3.	Reducer

Note:
•	Hadoop should be installed and running properly.
•	JAVA should be installed and JAVA_HOME environment should be supplied properly.z

#Writing the Mapper :

Extending the Mapper class creates the WordCountMapper class and the map function is implemented by overriding the map method in the Mapper class. The mapper functions takes a key-value pair as an input and outputs a key-values pair as an output (The output is given through the context object ). The key value pair that the map function takes as an input and the key value pair that is given as an output need not be of the same type.

For instance in the WordCountMapper the input to the map method is a key-value pair where the key is the line number and the value is the line of text in the corresponding line (line_number, line_text). And outputs (word, 1) for each word it reads in the line.

The pseudo code for the map function is below

void map(file, text) {
	     foreach word in text.split() {
	            output(word, 1);
    }
    }




The actual java code for the map function is below

public static class WordCountMapper extends Mapper<Object, Text, Text, IntWritable> {

	 

	        private final static IntWritable one = new IntWritable(1);

	        private Text word = new Text();

	 

	        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

	            StringTokenizer itr = new StringTokenizer(value.toString());

	            while (itr.hasMoreTokens()) {

	                word.set(itr.nextToken());

	                context.write(word, one);

	            }

	        }

	    }


#Writing the Reducer:

The WordCountReducer class is created by extending the org.apache.hadoop.mapreduce.Reducer class and the reduce method is implemented by overriding the reduce method from the Reducer class. The reduce function collects all the intermediate key-value pairs generated by the multiple map functions and will sum up all the occurrences of each word and output a key-value pair for each word in the text documents as. The detailed implementation of the WordCountReducer is below

The pseudo code for the reducer function is below 

void reduce(word, list(count)) {

	       output(word, sum(count));

	   }

The actual java code for the reducer function is below.

public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

	        private IntWritable result = new IntWritable();

	 

	        public void reduce(Text key, Iterable<IntWritable> values, Context context)

	                throws IOException, InterruptedException {

	            int sum = 0;

	            for (IntWritable val : values) {

	                sum += val.get();

	            }

	            result.set(sum);

	            context.write(key, result);

	        }

	    }



#Writing Main Method

The main method sets up all necessary configurations and runs the mapreduce job.

  1. Job Name : name of this Job
  2. Executable (Jar) Class: the main executable class. For here, WordCount.
  3. Mapper Class: class which overrides the "map" function. For here,  WordCountMapper.
  4. Reducer: class which override the "reduce" function. For here , WordCountReducer.
  5. Output Key: type of output key. For here, Text.
  6. Output Value: type of output value. For here, IntWritable.
  7. File Input Path
  8. File Output Path


Thanks in advance 
