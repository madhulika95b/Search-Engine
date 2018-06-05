import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class ReadQuery {

	public static final String TOPIC_NUM = "<num> Number:";
	public static final String TOPIC_TITLE = "<title>";
	private List<String> stopList;
	private Porter porter;

	public ReadQuery() {
		stopList = new ArrayList<>();
		porter = new Porter();
	}

	public static void main(String[] args) {
		ReadInputFiles readInputFiles = new ReadInputFiles();

		ReadQuery rq = new ReadQuery();
		rq.stopList = readInputFiles.loadStopList("./src/stopwordlist.txt");
		readInputFiles.setStopList(rq.stopList);
		readInputFiles.loadData("./src/ft911/");

		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(new File(
					"./src/output.txt")));
			HashMap<Integer, String> s1 = new HashMap<Integer, String>();
			Scanner query = new Scanner(System.in);
			try {
				query = new Scanner(new File("./src/topics.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			while (query.hasNextLine()) {
				String querys2 = query.nextLine();
				if (querys2.startsWith("<num>")) {
					int num = Integer.parseInt(querys2.substring(14, 17));
					querys2 = query.nextLine();
					String que = querys2.substring(8).toLowerCase();
					s1.put(num, que);
				}
			}
			Map<Integer, TreeMap<Integer, Double>> quedoc = new HashMap<Integer, TreeMap<Integer, Double>>();
			TreeMap<Integer, Double> store;
			String tokens[];
			double doc[] = new double[readInputFiles.getFrwdIndex().size() + 1];
			double num, denominator, quy, que1;
			int document;

			for (Map.Entry<Integer, String> entry : s1.entrySet()) {
				tokens = entry.getValue().split(" ");
				doc = new double[readInputFiles.getFrwdIndex().size() + 1];
				store = new TreeMap<Integer, Double>();
				for (String s : tokens) {
					int counter = 0;

					for (int i = 0; i < tokens.length; i++) {
						if (s.equals(tokens[i]))
							++counter;
					}

					que1 = counter / (Math.sqrt(tokens.length));
					String word = rq.porter.stripAffixes(s);

					if (readInputFiles.getWordDict().containsKey(word)) {
						for (Map.Entry<Integer, Integer> l1 : readInputFiles
								.getInvertedIndex()
								.get(readInputFiles.getWordDict().get(word))
								.entrySet()) {
							num = l1.getValue();
							denominator = 0;
							document = l1.getKey();

							for (Map.Entry<Integer, Integer> l2 : readInputFiles
									.getFrwdIndex().get(l1.getKey()).entrySet()) {
								denominator = Math.pow(l2.getValue(), 2)
										+ denominator;
							}
							quy = num / (Math.sqrt(denominator));
							doc[document] = que1 * quy + doc[document];
							store.put(document, doc[document]);
						}
					}

				}
				if (store.size() != 0)
					quedoc.put(entry.getKey(), store);
				else {
					store.put(0, 0.0);
					quedoc.put(entry.getKey(), store);
				}

			}

			for (Map.Entry<Integer, TreeMap<Integer, Double>> rnk : quedoc
					.entrySet()) {
				sortScores(rnk.getValue(), rnk.getKey(), 0, wr,
						readInputFiles.getFileDict());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void sortScores(TreeMap<Integer, Double> sortingmap,
			int queryNumber, int ranking, BufferedWriter wr1,
			Map<String, Integer> fileIndex) {

		List<Entry<Integer, Double>> list_store = new LinkedList<>(
				sortingmap.entrySet());
		Collections.sort(list_store, new Comparator<Entry<Integer, Double>>() {
			@Override
			public int compare(Map.Entry<Integer, Double> ob1,
					Entry<Integer, Double> ob2) {
				return (ob2.getValue()).compareTo(ob1.getValue());
			}
		});
		try {
			for (Map.Entry<Integer, Double> entry : list_store) {
				for (Entry<String, Integer> fileEntry : fileIndex.entrySet()) {
					if (fileEntry.getValue().equals(entry.getKey())) {
						// System.out.println("inside");
						wr1.write(queryNumber + " \t " + fileEntry.getKey()
								+ " \t " + (++ranking) + " \t "
								+ entry.getValue());
						wr1.newLine();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
