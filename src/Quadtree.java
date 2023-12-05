import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Quadtree{


    private int size;
    private int max_lumi;

    private QuadtreeNode racine;

    //constructeur 
    public Quadtree(QuadtreeNode racine, int size, int max_lumi){
        this.racine = racine;
        this.size = size;
        this.max_lumi = max_lumi;
    }

    //charge l'image PGM et construit le quadtree correspondant
    public Quadtree(String FilePath) throws FileNotFoundException{ 

        //lecture du fichier PGM et initialisation de la matrice 
        int[][] mat_Nodes = readPGMFile(FilePath);
        //construction de l'arbre
        racine = buildQuadtree(mat_Nodes, 0, this.size-1 , 0, this.size-1);
    }

    public QuadtreeNode buildQuadtree(int[][] matrix, int debLigne, int finLigne, int debCol, int finCol){
        if(debLigne == finLigne && debCol == finCol)
            return new QuadtreeNode(matrix[debLigne][debCol], true);
        //if(finLigne-debLigne == finCol-debCol && matrix[debLigne][debCol] == matrix[debLigne][debCol+1] && matrix[debLigne][debCol+1] == matrix[debLigne+1][debCol] && matrix[debLigne][debCol+1] == matrix[debLigne+1][debCol+1])
            //return new QuadtreeNode(matrix[debLigne][debCol], true);
        
        int milieuLigne = (debLigne+finLigne)/2;
        int milieuCol = (debCol+finCol)/2;

        QuadtreeNode fils1 = buildQuadtree(matrix, debLigne, milieuLigne, debCol, milieuCol);
        QuadtreeNode fils2 = buildQuadtree(matrix, debLigne, milieuLigne, milieuCol+1, finCol);
        QuadtreeNode fils3 = buildQuadtree(matrix, milieuLigne+1, finLigne, milieuCol+1, finCol);
        QuadtreeNode fils4 = buildQuadtree(matrix, milieuLigne+1, finLigne, debCol, milieuCol);

        if(fils1.isLeaf() && fils2.isLeaf() && fils3.isLeaf() && fils4.isLeaf() && fils1.getValue()== fils2.getValue() && 
            fils1.getValue()==fils3.getValue() &&  fils1.getValue()== fils4.getValue())
            return new QuadtreeNode(fils1.getValue(), true);

        return new QuadtreeNode(0, false,fils1, fils2, fils3, fils4);
    }


    //lecture du fichier PGM et retourne la matrice correspondante
    public int[][] readPGMFile(String filename) throws FileNotFoundException{
            Scanner scanner = new Scanner(new File(filename));
            scanner.nextLine(); // 'P2'
            scanner.nextLine(); // commentaire    
            String[] tsize = scanner.nextLine().split((" "));
            this.size = Integer.parseInt(tsize[0]);
            this.max_lumi = Integer.parseInt(scanner.nextLine()); //liminosite max
            int[][] matrix = new int[size][size];
            for(int i = 0; i < size; i++){
                //String[] line = scanner.nextLine().split((" "));
                for(int j=0; j < size; j++){
                    matrix[i][j] = Integer.parseInt(scanner.next());
                }
            }
            scanner.close();
            return matrix;

    }

    public int getSize(){ return this.size; }
    public int getLumMax(){ return this.max_lumi; }
    public QuadtreeNode getRacine(){    return this.racine; }

    public void setRacine(QuadtreeNode newracine){ this.racine = newracine; }

    //affichage d'un noeud de l'arbre
    public String toString(){
        if(racine == null)
            return"()";
        else if(racine.isLeaf())
            return "(" + racine.getValue() + ")";
        else 
            return "("+racine.getFils1().toString()+racine.getFils2().toString()+ racine.getFils4().toString() + racine.getFils3().toString()+")";
    }

    //Methode qui génère à l'endroit path un fichier PGM qui correspond au Quadtree
    public void toPGM(String path) throws IOException { 
        //on va d'abord stoquer les valeurs du quadtree dans une matrice
        //ne pas stoquer la valeur de la racine qui ne correspond à aucune luminosité
        int[][] matrix = new int[this.size][this.size];
        fillMatrixFromQuadtree(matrix, this.racine, 0, 0, this.size-1, this.size-1);

        //Ensuite ecrire la matrice dans le fichier pgm
        try(BufferedWriter ecrire = new BufferedWriter(new FileWriter(path))){
            ecrire.write("P2\n");
            ecrire.write("#generation du pgm\n");
            ecrire.write(this.size + " " + this.size + "\n");
            ecrire.write(this.max_lumi + "\n");

            for(int i=0; i<this.size; i++){
                for(int j=0; j<this.size; j++){
                    ecrire.write(matrix[i][j] + " ");
                }
                ecrire.write("\n");
            }
        }
        System.out.println("Fichier PGM généré avec succés !");
    }

    //mettre l'arbre dans une matrice 
    public void fillMatrixFromQuadtree(int[][] matrix, QuadtreeNode noeud, int debLigne, int debCol, int finLigne, int finCol){
        if(noeud != null){
            if(noeud.isLeaf()){ //si feuille
                for (int i = debLigne; i <= finLigne; i++) {
                    for (int j = debCol; j <= finCol; j++) {
                        matrix[i][j] = noeud.getValue();
                    }
                }
            }else{

                int milieuLigne = (debLigne+finLigne)/2;
                int milieuCol = (debCol+finCol)/2;

                fillMatrixFromQuadtree(matrix, noeud.getFils1(), debLigne, debCol, milieuLigne, milieuCol);
                fillMatrixFromQuadtree(matrix, noeud.getFils2(), debLigne, milieuCol+1, milieuLigne, finCol);
                fillMatrixFromQuadtree(matrix, noeud.getFils3(), milieuLigne+1, milieuCol+1, finLigne, finCol);
                fillMatrixFromQuadtree(matrix, noeud.getFils4(), milieuLigne+1, debCol, finLigne, milieuCol);
            }
        }
    }

    //Methode qui compresse le Quadtree selon la premiere technique 2.3.1
    public Quadtree compressLambda(){ 
        //cloner l'arbre initial
        QuadtreeNode copie = cloneTree(this.racine);
        Quadtree compressQuad = new Quadtree(copie, this.size/2, this.max_lumi);
        if(compressQuad.racine.isLeaf())
            return new Quadtree(copie, 1, max_lumi);
        else if(compressQuad.racine.isBrindille()){
                int compressedValue = mayenneLogarithmique(copie);
                return new Quadtree(new QuadtreeNode(compressedValue,true), 1, this.max_lumi);
        }else{
            compressQuad.racine.setFils1(compressLambdaNode(compressQuad.racine.getFils1()));
            compressQuad.racine.setFils2(compressLambdaNode(compressQuad.racine.getFils2()));
            compressQuad.racine.setFils3(compressLambdaNode(compressQuad.racine.getFils3()));
            compressQuad.racine.setFils4(compressLambdaNode(compressQuad.racine.getFils4()));
            if(compressQuad.racine.allCompressedTreeEqual()){
                    QuadtreeNode newNode = new QuadtreeNode(compressQuad.racine.getFils1().getValue(), true);
                    return  new Quadtree(newNode, this.size, max_lumi);
            }else
                return compressQuad;
        }
    }

    private QuadtreeNode compressLambdaNode(QuadtreeNode node){
        if(node.isLeaf()){//une feuille
            return node;
        }else if(node.isBrindille()){ //une brindille donc on compresse
                int compressedValue = mayenneLogarithmique(node);
                return new QuadtreeNode(compressedValue, true);
        }else{//un noeud interne
                node.setFils1(compressLambdaNode(node.getFils1()));
                node.setFils2(compressLambdaNode(node.getFils2()));
                node.setFils3(compressLambdaNode(node.getFils3()));
                node.setFils4(compressLambdaNode(node.getFils4()));
                if(node.allCompressedTreeEqual()){
                    QuadtreeNode newNode = new QuadtreeNode(node.getFils1().getValue(), true);
                    return  newNode;
                }else
                    return node;
            }
    }

    //methode pour compresser une brindille
    private int mayenneLogarithmique(QuadtreeNode node){
        //calculer la moyenne logarthmique de luminosite Λ
        double SumLambda = 0.0;
        for(int i=1; i<=4; i++){
            double y_i = node.getFils(i).getValue();
            SumLambda += Math.log(0.1 + y_i);
        }
        double Λ = Math.exp(0.25*SumLambda);
        int compressedValue = (int)Math.round(Λ);
        return compressedValue;
    }

    //methode pour cloner un arbre 
    private QuadtreeNode cloneTree(QuadtreeNode node){
        if(node == null)
            return null;
        else if(node.isLeaf())
            return new QuadtreeNode(node.getValue(), true);
        else 
            return new QuadtreeNode(0, false,
            cloneTree(node.getFils1()), cloneTree(node.getFils2()),
            cloneTree(node.getFils3()), cloneTree(node.getFils4()));
    }
    

    public Quadtree compressRho(int rho) {
        int inialNode = countNodes();
        //copie de l'arbre
        QuadtreeNode compressQuadtree = cloneTree(racine);

        // Obtenez la liste des brindilles et de leurs écarts
        List<Double> Ecarts = findEcarts(this.racine);

        // Triez la liste par ordre croissant d'écart
        Collections.sort(Ecarts);
        
        //compresser si tauxCompression > p%
        double tauxCompress = compressQuadtree.countNodes()/inialNode;
        while(tauxCompress > rho){
            // Appliquez la compression uniquement sur le nœud avec le plus petit écart
            if (!Ecarts.isEmpty()) {
                double petitEcart = Ecarts.get(0);
                //chercher la brindille qui correspond à cette ecart la
                //stoquer le parent 
                QuadtreeNode parent = compressQuadtree;
                //compresser la brindille
                
                //si le parent deviens une brindille il peut etre compresser s'il est plus petit que petitEcart
                if (parent.isBrindille()) {
                    double ecartParent = calculateCompressionImpcat(parent);
                    if (ecartParent < petitEcart) {
                        parent.setIsLeaf(true);
                        parent.setValue(mayenneLogarithmique(parent));
                    }
                }
            }
            tauxCompress = compressQuadtree.countNodes()/inialNode;
        }
        return new Quadtree(compressQuadtree, this.size, this.max_lumi);
    }

    // Modifiez la méthode findBrindilles pour renvoyer une liste de BrindilleAvecEcart
    private List<Double> findEcarts(QuadtreeNode node) {
        List<Double> brindillesAvecEcarts = new ArrayList<>();
        findEcartsRecursive(node, brindillesAvecEcarts);
        return brindillesAvecEcarts;
    }

    private void findEcartsRecursive(QuadtreeNode node, List<Double> Ecarts) {
        if (node != null && node.isBrindille()) {
            double ecart = calculateCompressionImpcat(node);
            Ecarts.add(ecart);
        } else if (node != null && !node.isLeaf()) {
            findEcartsRecursive(node.getFils1(), Ecarts);
            findEcartsRecursive(node.getFils2(), Ecarts);
            findEcartsRecursive(node.getFils3(), Ecarts);
            findEcartsRecursive(node.getFils4(), Ecarts);
        }
    }

    //precondition node est une brindille
    private double calculateCompressionImpcat(QuadtreeNode node){
        double maxEcart = Double.MIN_VALUE;
        double Λ = mayenneLogarithmique(node);

        for(int i=1; i<=4; i++){
            double lambda_i = node.getFils(i).getValue();
            double ecart = Math.abs(Λ-lambda_i);
            maxEcart = Math.max(maxEcart, ecart);
        }
        return maxEcart;
    }

    //Calculer le nombre de noeud du quadtree
    public int countNodes(){
        if(this.racine == null)
            return 0;
        else if(this.racine.isLeaf())//une feuille
                return 1;
            else{
                int cpt = 1;
                cpt += racine.getFils1().countNodes();
                cpt += racine.getFils2().countNodes();
                cpt += racine.getFils3().countNodes();
                cpt += racine.getFils4().countNodes();
                return cpt;
            }
    }


    
}