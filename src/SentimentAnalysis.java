/**
 * Created by bobby on 11/17/16.
 */


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

public class SentimentAnalysis  {

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.addResource(new Path("/Users/bobby/Downloads/hadoop/conf/core-site.xml"));
        DistributedCache.addCacheFile(new URI("hdfs://127.0.0.1:8020/tmp/AFINN-111"),conf);
        Job job = Job.getInstance(conf, "Sentiment");

        job.setJarByClass(SentimentAnalysis.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(Reducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }


    public static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private URI[] files;

        private HashMap<String, String> AFINN_map = new HashMap<String, String>();

        @Override
        public void setup(Context context) throws IOException {
            files = DistributedCache.getCacheFiles(context.getConfiguration());
            Path path = new Path(files[0]);
            FileSystem fs = FileSystem.get(context.getConfiguration());
            FSDataInputStream in = null;
            try {
                in = fs.open(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println("opened");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null)
            {
                String splits[] = line.split(" ");

                AFINN_map.put(splits[0], splits[1]);
            }

            br.close();
            in.close();

        }

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            int index = line.indexOf("in_reply_to_status_id_str");
            if (index == -1) {
                return;
            }
            line = "{"+line.substring(index - 1);
            System.out.println(line);
            JSONParser jsonParser = new JSONParser();
            JSONObject obj = null;
            try {
                obj = (JSONObject) jsonParser.parse(line);
            } catch (ParseException e) {
                //System.out.println();
                //e.printStackTrace();
                return;
            }
            String tweet_id = (String) obj.get("id_str");
            String tweet_text = (String) obj.get("text");
            String[] splits = tweet_text.toString().split("\\t");
            int sentiment_sum = 0;
            System.out.println("tweet id = "+tweet_id);
            System.out.println("tweet text = "+tweet_text);
            for (String word : splits) {
                if (AFINN_map.containsKey(word)) {
                    Integer x = new Integer(AFINN_map.get(word));
                    sentiment_sum += x;
                }
            }
            context.write(new Text(tweet_id), new IntWritable(sentiment_sum));
        }
    }

}