package it.polito.softeng.widgets;

import java.io.*;
import java.util.*;
//import org.apache.commons.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Main {

    public static final String ORACLE_PATH = "C:\\Users\\tomma\\Desktop\\Multilocator\\oracles.csv";
    public static final String ALL_ORACLE_PATH = "";
    public static boolean booleana = false;
    final static String FULL_HEADER = "package,checkable,clickable,content-desc,index,focusable,enabled,resource-id,password,NAF,bounds,focused,checked,long-clickable,text,class,scrollable,selected";
    final static String INTESTAZIONE_NO_BOOLEAN = "package,content-desc,index,resource-id,bounds,text,NAF,class";

    final static String INTESTAZIONE_BOOLEAN = "checkable,clickable,focusable,enabled,password,focused,checked,long-clickable,scrollable,selected";
    private static final String dirPath = "C:\\Users\\tomma\\Desktop\\Multilocator\\xmls";
    public static void main(String[] args) {

        Map<String, List<TreeMap<String, Integer>>> oracoli = getOracleListForAllApplication();
        //List<TreeMap<String, Integer>> oracoli = getOracleListForAllApplication();

        File dir = new File(dirPath);
        File[] dirList = dir.listFiles();
        List<Map<String, Integer>> cambiAttributi = new ArrayList<>();
        HashMap<String, List<Integer>> m3Full = new HashMap<>();
        if(dirList != null) {
            HashMap<String, Integer> m1Full = new HashMap<>();
            Map<String,Integer> variazioniAttrPerWidget = new TreeMap<>();

            for (File child : dirList) {
                File inputFile = null;
                if(!child.getName().contains("csv") || !child.getName().contains("new.")){
                    continue;
                } else {
                    if(child.getName().contains("png") || child.getName().contains("xml"))
                        continue;
                    inputFile = child;
                }

                String outputName = child.getName().replace(".csv", "_comparison.txt");

                //variabilita(inputFile,new File(outputName));

                File inputFile2 = new File(inputFile.getAbsolutePath().replace("new.","old."));
                String app = inputFile.getName().split("_new\\.")[0];
                /*System.out.println("input new file: " + inputFile.getName() + ", input old file: " + inputFile2.getName() +
                        ", output file name: " + outputName + " - oracoli dell'app: " + app);*/

                List<HashMap> res = quantoDiverso(inputFile, inputFile2,new File(outputName), null);

                HashMap<String, Integer> m1 = res.get(0);
                HashMap<Integer, Integer> m2 = res.get(1);
                HashMap<String, List<Integer>> m3 = res.get(2);


                cambiAttributi.add(m1);
                for(String attr : m1.keySet()){
                    Integer val = m1Full.get(attr);
                    if(val == null){
                        m1Full.put(attr, m1.get(attr));
                    } else {
                        val += m1.get(attr);
                        m1Full.put(attr, val);
                    }
                }

                for(String attr : m3.keySet()){
                    List<Integer> val = m3.get(attr);
                    /*if(val == null){
                        m1Full.put(attr, m3.get(attr));
                    } else {
                        val += m1.get(attr);
                        m1Full.put(attr, val);
                    }*/
                    m3Full.put(attr, val);
                }

                for(Integer old : m2.keySet()){
                    Integer val = m2.get(old);
                    variazioniAttrPerWidget.put(app+"_"+old, val);
                }
            }
            //System.out.println(m1Full);
            //System.out.println(variazioniAttrPerWidget);
            int changes = 0;
            for(String chiave:variazioniAttrPerWidget.keySet()){
                changes += variazioniAttrPerWidget.get(chiave);
            }
            System.out.print("Totale (dividendo): " + changes);
            System.out.print(" Divisore " + variazioniAttrPerWidget.size());
            System.out.println(" Media " + (double) changes/variazioniAttrPerWidget.size());
        }

        File statistiche = new File("statistiche_variabilita.csv");
        try {
            FileWriter fwStatistiche= new FileWriter(statistiche,false);
            //fwStatistiche.write("appname,"+intestazione);
            //fwStatistiche.write(System.lineSeparator());
            fwStatistiche.write("app;attribute;total;empty;values;diffvalues;changes"+System.lineSeparator());
            fwStatistiche.close();
        } catch( Exception e){

        }

        /*File statistiche2 = new File("statistiche_variabilita_num_assoluti.csv");
        try {
            FileWriter fwStatistiche= new FileWriter(statistiche2,false);
            fwStatistiche.write("appname,"+intestazione);
            fwStatistiche.write(System.lineSeparator());
            fwStatistiche.close();
        } catch( Exception e){

        }*/


        int i = 0;
        for (File child : dirList) {
            File inputFile = null;
            if(!child.getName().contains("csv")){
                continue;
            } else {
                //if(child.getName().contains("_new."))
                    inputFile = child;
                //else
                //    continue;
            }

            //variabilita(inputFile,new File(outputName));

            String oName = child.getName().replace(".csv", "_variability.txt");
            variabilita(m3Full,cambiAttributi, i,inputFile, new File(oName), statistiche);
            i++;
        }
    }

    public static List<HashMap> quantoDiverso(File inputFileNew, File inputFileOld, File outputFile, List<List<Integer>> oracoli){
        List<HashMap> toReturn = new ArrayList<>();
        try {

            FileWriter fw = new FileWriter(outputFile);

            CSVFormat format = CSVFormat.newFormat(',').withHeader();

            Reader fileNew = new BufferedReader(new FileReader(inputFileNew));
            CSVParser parserNew = new CSVParser(fileNew, format);

            Reader fileOld = new BufferedReader(new FileReader(inputFileOld));
            CSVParser parserOld = new CSVParser(fileOld, format);


            Set<String> columns = new HashSet<>();//parser.getHeaderMap().keySet();
            HashMap<String, HashMap<String, Integer>> variabilita = new HashMap<>();

            List<CSVRecord> listOld = parserOld.getRecords();
            Map<String, Integer> headerMap = parserOld.getHeaderMap();

            List<CSVRecord> listNew = parserNew.getRecords();

            columns = new HashSet<>(headerMap.keySet());

            String toPrint = inputFileOld + " -> "+ inputFileOld + System.lineSeparator();

            HashMap<String, Integer> cambiPerAttributi = new HashMap<>();
            HashMap<Integer, Integer> widgetCambiato = new HashMap<>();
            HashMap<String, List<Integer>> modificheVisive = new HashMap<>();


            for (CSVRecord record : listOld) {
                for(List<Integer> coppia :oracoli){
                    int progressId = Integer.parseInt(record.get("progress").trim());

                    if(progressId == coppia.get(0)){

                        Integer attributiCambiati = widgetCambiato.get(progressId);
                        if(attributiCambiati == null){
                            attributiCambiati = 0;
                        }

                        // è match, quindi è un oracolo
                        CSVRecord recordNuovo = listNew.get(coppia.get(1));
                        //System.out.println(record.get("resource-id") + " --> " + recordNuovo.get("resource-id"));

                        int count = 0;
                        boolean dirty = false;
                        //HashMap<String >

                        for (String colonna : columns) {

                            String vecchiovalore = record.get(colonna);
                            String nuovovalore = recordNuovo.get(colonna);
                            if(!vecchiovalore.equals(nuovovalore) && !colonna.equalsIgnoreCase("progress")){
                                Integer num = cambiPerAttributi.get(colonna);
                                if(num == null){
                                    num = 0;
                                }
                                cambiPerAttributi.put(colonna, ++num);
                                attributiCambiati++;
                                toPrint += "in widget number "+ record.get("progress") +", attribute " + colonna + " changed value from " + vecchiovalore + " to " + nuovovalore +System.lineSeparator();
                                dirty = true;//break;
                            } //else {

                            if(++count == columns.size()){
                                if(!dirty) {
                                    System.out.println("GODO");
                                } else {

                                }

                            }
                            //}
                        }

                        if(attributiCambiati!=0)
                            widgetCambiato.put(progressId,attributiCambiati);

                        List<Integer> daAggiungere = new ArrayList<>();
                        for(int i = 2; i < coppia.size(); i++){
                            daAggiungere.add(coppia.get(i));
                        }
                        modificheVisive.put(inputFileNew + "-"+progressId,daAggiungere);
                    }
                }


                /*for (String colonna : columns) {
                    HashMap<String, Integer> tmp = variabilita.get(colonna);
                    if (tmp == null) {
                        tmp = new HashMap<String, Integer>();
                    }
                    String valore = record.get(colonna);
                    Integer ripetizioni = tmp.get(valore);
                    if (ripetizioni == null) {
                        ripetizioni = 0;
                    }
                    tmp.put(valore, ++ripetizioni);
                    variabilita.put(colonna, tmp);
                }*/
            }

            toReturn.add(cambiPerAttributi);
            toReturn.add(widgetCambiato);
            toReturn.add(modificheVisive);
            fw.write(toPrint);
            fw.write(System.lineSeparator() + cambiPerAttributi);
            fw.write(System.lineSeparator() + widgetCambiato);
            fw.flush();
            //System.out.println(toPrint);
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }

        return toReturn;
    }

    public static void variabilita(Map<String, List<Integer>> modificheVisive, List<Map<String, Integer>> liston, int count, File inputFile, File outputFile, File statistiche){
        try {

            FileWriter fwStatistiche= new FileWriter(statistiche,true);
            FileWriter fw = new FileWriter(outputFile);

            //File inputFile = new File();
            //Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\tomma\\Desktop\\Multilocator\\xmls\\01_art_01_sketchbook_new.csv"));//(dirPath+"\\01_art_01_sketchbook_new.csv"));
            Reader reader = new BufferedReader(new FileReader(inputFile));
            CSVFormat format = CSVFormat.newFormat(',').withHeader();
            CSVParser parser = new CSVParser(reader, format);

            //List lista = parser.getHeaderNames();

            Set<String> columns = new HashSet<>();//parser.getHeaderMap().keySet();
            HashMap<String, HashMap<String, Integer>> variabilita = new HashMap<>();

            List<CSVRecord> l = parser.getRecords();
            Map<String, Integer> headerMap = parser.getHeaderMap();

            columns = new HashSet<>(headerMap.keySet());

            /*CSVRecord recZero = l.get(0);
            HashMap<Integer, String> indexColumn  = new HashMap<>();

            columns = new HashSet<>(recZero.toList());

            recZero.stream().iterator().forEachRemaining((c, index, o) -> {
                columns.add(c);
                indexColumn.put(index, c);
            });*/

            System.out.println("Ecco le colonne:  -- " + columns);

            for (CSVRecord record : l) {
                for (String colonna : columns) {
                    HashMap<String, Integer> tmp = variabilita.get(colonna);
                    if (tmp == null) {
                        tmp = new HashMap<String, Integer>();
                    }
                    String valore = record.get(colonna);
                    Integer ripetizioni = tmp.get(valore);
                    if (ripetizioni == null) {
                        ripetizioni = 0;
                    }
                    tmp.put(valore, ++ripetizioni);
                    variabilita.put(colonna, tmp);
                }
            }

            for (String colonna : columns) {
                fw.write(colonna + " --> ");
                fw.write(variabilita.get(colonna).toString() + System.lineSeparator());
            }

            /*String[] colonne = intestazione.split(",");
            fwStatistiche.write(inputFile.getName());
            for(String colonna : colonne){
                HashMap temp = variabilita.get(colonna);
                int t = variabilita.get(colonna).size();

                //fwStatistiche.write(",="+variabilita.get(colonna).size();+"/"+l.size());
                fwStatistiche.write(","+t);
            }
            fwStatistiche.write(","+l.size());
            fwStatistiche.write(System.lineSeparator());
            */


            //GENERAZIONE DEL FILE CSV DA FAR GIRARE IN R
            //String[] colonne = intestazione.split(",");
            String[] colonne = booleana ? INTESTAZIONE_BOOLEAN.split(",") : INTESTAZIONE_NO_BOOLEAN.split(",");
            //fwStatistiche.write(inputFile.getName());

            //GENERAZIONE DELLE STATISTICHE NON BOOLEAN
            if(!booleana){
                Map<String, Integer> mappetta = liston.get((int)(count/2));
                for(String colonna : colonne){
                    //for()
                    fwStatistiche.write(count+";"+colonna+";"+l.size()+";");
                    HashMap temp = variabilita.get(colonna);

                    int emptyValue = variabilita.get(colonna).get("") == null ? 0 : variabilita.get(colonna).get("");

                    int t = variabilita.get(colonna).size();

                    //fwStatistiche.write(",="+variabilita.get(colonna).size();+"/"+l.size());
                    fwStatistiche.write(emptyValue+";"+(l.size()-emptyValue)+ ";"+ (emptyValue != 0 ? t-1 : t));

                    int numeroCambiamentiOracoli = mappetta.get(colonna) != null ? mappetta.get(colonna) : 0;
                    fwStatistiche.write(";" + numeroCambiamentiOracoli);

                    fwStatistiche.write(System.lineSeparator());
                }
            } else {

                //GENERAZIONE DELLE STATISTICHE BOOLEAN
                Map<String, Integer> mappetta = liston.get((int) (count / 2));
                for (String colonna : colonne) {
                    fwStatistiche.write(count + ";" + colonna + ";" + l.size() + ";");
                    HashMap temp = variabilita.get(colonna);

                    int falseValue = variabilita.get(colonna).get("false") == null ? 0 : variabilita.get(colonna).get("false");

                    int t = variabilita.get(colonna).size();

                    //fwStatistiche.write(",="+variabilita.get(colonna).size();+"/"+l.size());
                    fwStatistiche.write(falseValue + ";" + (l.size() - falseValue) + ";" + t);

                    int numeroCambiamentiOracoli = mappetta.get(colonna) != null ? mappetta.get(colonna) : 0;
                    fwStatistiche.write(";" + numeroCambiamentiOracoli);

                    fwStatistiche.write(System.lineSeparator());
                }
            }

            /*fwStatistiche.write(","+l.size());
            fwStatistiche.write(System.lineSeparator());*/
            fw.close();
            fwStatistiche.close();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    public static Map<String, List<TreeMap<String,Integer>>> getOracleListForAllApplication(){
        Map<String,List<TreeMap<String,Integer>>> allOracles = new HashMap<>();
        File f = new File(ALL_ORACLE_PATH);
        try {
            Reader file = new BufferedReader(new FileReader(f));
            CSVFormat format = CSVFormat.newFormat(',').withHeader();
            CSVParser parser = new CSVParser(file, format);

            List<CSVRecord> l = parser.getRecords();
            Map<String, Integer> headerMap = parser.getHeaderMap();

            //int count = 0;
            for (CSVRecord record : l) {

                //THIS DEALS WITH VISUAL CHANGES
                /*String[] change_list = record.get("change_type")!= null ? record.get("change_type").split(";") : null;
                List<Integer> cambioLista= new ArrayList<>();
                for(String s :change_list) {
                    cambioLista.add(Integer.parseInt(s));

                }*/



                String app = record.get("app");
                //get the list of oracles if it was already presente, otherwise create a new List
                List<TreeMap<String, Integer>> oracleListForAnApp = allOracles.get(app) != null ? allOracles.get(app) : new ArrayList<>();

                TreeMap<String, Integer> changesMap = new TreeMap<>();

                for(String version : headerMap.keySet()){
                    //CHECK IF THE COLUMN IS ACTUALLY A VERSION, OR ANOTHER DESCRIPTIVE COLUMN, might be unnecessary
                    if(!version.equalsIgnoreCase("app") && !version.equalsIgnoreCase("comment") && !version.equalsIgnoreCase("change_type")) {
                        String value = record.get(version);
                        if(value != null)
                            changesMap.put(version, Integer.parseInt(value));
                    }
                }
                oracleListForAnApp.add(changesMap);

                allOracles.put(app, oracleListForAnApp);
            }

        } catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getClass() + " - " + e.getMessage());

        }
        return allOracles;
    }

    public static List<TreeMap<String,Integer>> getOracleListForAnApp(){
        List<TreeMap<String,Integer>> oracles = new ArrayList<>();
        File f = new File(ORACLE_PATH);
        try {
            Reader file = new BufferedReader(new FileReader(f));
            CSVFormat format = CSVFormat.newFormat(',').withHeader();
            CSVParser parser = new CSVParser(file, format);

            List<CSVRecord> l = parser.getRecords();
            //THIS IS THE MAP WITH THE HEADER OF THE CSV
            Map<String, Integer> headerMap = parser.getHeaderMap();

            //WE ITERATE OVER THE FULL LIST OF ORACLES
            for (CSVRecord record : l) {

                //VISUAL MUTATION CHANGE TYPE NOT ACTIVATED
                /*String[] change_list = record.get("change_type")!= null ? record.get("change_type").split(";") : null;
                List<Integer> cambioLista= new ArrayList<>();
                for(String s :change_list) {
                    cambioLista.add(Integer.parseInt(s));

                }*/

                //GET THE NAME OF THE APP, MAYBE NOT USEFUL IF THE CSV CONTAINS ONLY DATA FOR ONE APP
                String app = record.get("app");

                //INITIALIZE THE CHANGES MAP, HERE WE TRACK ALL THE INDEXES AN ORACLE HAS IN EACH VERSION
                //THE KEY IS THE VERSION, THE VALUE IS THE INDEX
                TreeMap<String, Integer> changesMap = new TreeMap<>();

                for(String version : headerMap.keySet()){
                    //CHECK IF THE COLUMN IS ACTUALLY A VERSION, OR ANOTHER DESCRIPTIVE COLUMN, might be unnecessary
                    if(!version.equalsIgnoreCase("app") && !version.equalsIgnoreCase("comment") && !version.equalsIgnoreCase("change_type")) {
                        String value = record.get(version);
                        if(value != null)
                            changesMap.put(version, Integer.parseInt(value));
                    }
                }

                oracles.add(changesMap);

                String x = record.get("old_node");
                String y = record.get("new_node");


                //System.out.println("cunt:"+ ++count+ "{" +x + " - "+  y +"}");
                //Map.Entry<Integer, Integer> c = new AbstractMap.SimpleEntry<Integer, Integer>(Integer.parseInt(x),Integer.parseInt(y));
            }

        } catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getClass() + " - " + e.getMessage());

        }
        return oracles;
    }

    /*public static class Coppia {
        int vecchio;
        int nuovo;
        public Coppia(int v, int n){
            vecchio = v;
            nuovo = n;
        }
        public int getVecchio() {
            return vecchio;
        }
        public int getNuovo() {
            return nuovo;
        }
    }*/
}
