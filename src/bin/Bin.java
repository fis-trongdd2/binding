package bin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Bin {
	public static final String MEANS = "MEANS";
	public static final String BOUNDARIES = "BOUNDARIES";
	public static List<String> dictionary;
	public static String link;
	//get dictionary
	public static List<String> getDictionary(String link) throws FileNotFoundException, UnsupportedEncodingException {
		List<String> listData = new ArrayList<String>();
		FileInputStream fi = new FileInputStream(link);
		InputStreamReader isr = new InputStreamReader(fi, "UTF8");
		try(BufferedReader br = new BufferedReader(isr)) {
			for(String line; (line = br.readLine()) != null; ) {
				listData.add(line.trim());
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return listData;
	}
	
	// read file to list <list> word: line is a list, list of lines is a list
	public static List<List<String>> readFileToListString (String link) throws FileNotFoundException, UnsupportedEncodingException {
		List<List<String>> listData = new ArrayList<>();
		FileInputStream fi = new FileInputStream(link);
		InputStreamReader isr = new InputStreamReader(fi, "UTF8");
		List<String> temp = null;
		try(BufferedReader br = new BufferedReader(isr)) {
			for(String line; (line = br.readLine()) != null; ) {
				temp = new ArrayList<>();
				temp.addAll(Arrays.asList(line.split(" ")));
				listData.add(temp);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return listData;
	}
	
	//sort map by value
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
		
	//resetFrequency based on binding
	public static Map<Integer,Integer> resetFrequency (Map<Integer,Integer> words,String type, int numberOfBin) {
		// sort asc
		words = sortByValue(words);
		List<Integer> values = new ArrayList<>(words.values());
		List<Integer> newvalues = new ArrayList<>();

		//bin : update frequency
		newvalues = binding(values,numberOfBin,type);
		int i = 0;
		for (Map.Entry<Integer, Integer> temp : words.entrySet()) {
			words.put(temp.getKey(), newvalues.get(i++));
		}
		//return with old index of line and new frequency value
		return  new TreeMap<Integer, Integer>(words);
	}
	
	//return new value in each bin
	public static List<Integer> computeValues(List<Integer> list,String type) {
		if (type == MEANS ) {
			double total = 0;
			for (int i : list) {
				total+= i;
			}
			Collections.fill(list,(int) Math.round(total/list.size()));
		} else {
			int begin = list.get(0);
			int length = list.size();
			int end = list.get(length - 1);
			double t = (double)(end + begin)/2;
			for (int i = 0; i < length; i++) {
				if (list.get(i) < t) {
					list.set(i,begin);
				}
				else {
					list.set(i, end);
				}
			}
		}
		return list;
	}

	//compute vector
	public static List<Integer> binding (List<Integer> list, int numberOfBin, String type) {
		int length = list.size();
		int range = (length%numberOfBin==0)?length/numberOfBin:length/numberOfBin+1;
		int i,j;
		List<Integer> temp =  null;
		List<Integer> result =  new ArrayList<>();
		
		for ( i = 0; i < length; i++) {
			j = 0;
			temp = new ArrayList<>();
			if(length - i < range) {
				range = (length - i);
			}
			while (j < range) {
				temp.add(list.get(i+j));
				j++;
			}
			result.addAll(computeValues(temp,type));
			i += j-1;
		}
		return result;
	}
	
	//return result
	public static List<List<Integer>> smooth ( List<List<String>> listwords,String type, int numberOfBin) throws FileNotFoundException, UnsupportedEncodingException {
		dictionary =  getDictionary(link);
		List<List<Integer>> result = new ArrayList<>();
		Map<Integer,Integer> temp = null;
		List<Integer> tempList = null;
		for (int i = 0; i < dictionary.size(); i++) {
			temp = new HashMap<>();
			for (int j = 0; j < listwords.size(); j++) {
				temp.put(j, countWord(dictionary.get(i),listwords.get(j)));
			}
			temp = resetFrequency(temp, type, numberOfBin);
			tempList = new ArrayList<>(temp.values());
			result.add(tempList);
		}
		return result;
	}

	//count frequency : a word in a list of words 
	public static int countWord(String word, List<String> words) {
		int count = 0;
		for (String string : words) {
			if (string.compareTo(word) == 0) {
				count++;
			}
		}
		return count;
	}
	
	//write text to file
	public static void writeToFileUtf8Buffer (String link,String  listData) {
		try {
			File file = new File(link);
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw =  new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			bw.write(listData );
			bw.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//main test
	public static void main (String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		//link : dictionary
		link = "input/word-list.txt";
		
		//link : file
		List<List<String>> words = readFileToListString("input/data1.txt");
		

		List<List<Integer>> result = smooth(words,BOUNDARIES,2);
//		for (List<Integer> temp  : result) {
//		System.out.println(temp);
//		}

		StringBuilder end = new StringBuilder();
		end.append("@relation gap\n\n");
		for (int i = 0; i < dictionary.size(); i++) {
			end.append("@attribute word_" + i+" numeric\n");
		}
		end.append("\n");
		int length = result.get(0).size();
		for (int i = 0; i < length; i++) {
			end.append("{");
			for (int j = 0; j < result.size(); j++) {
				end.append(j+" "+result.get(j).get(i)+",");
			}
			end.deleteCharAt(end.length()-1);
			end.append("}\n");
		}
		writeToFileUtf8Buffer("input/bin.arff", end.toString());
	}
}
