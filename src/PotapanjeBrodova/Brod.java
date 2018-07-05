package PotapanjeBrodova;


import java.util.List;
import java.util.Vector;


class Brod {

	List<Integer> lokacija;
    int ostatakBroda;
    
    public Brod(){
        lokacija=new Vector<>();
        ostatakBroda=0;
    }
    
    public Brod(int[] l){
        lokacija=new Vector<>();
        for (int i:l)
            lokacija.add(i);
        ostatakBroda=l.length;
    }
    
    /**umanjuje ostatakBroda ako je pogodjena neka od lokacija broda*/
    public boolean pogodak(int i){
        if (lokacija.contains(i)){
            ostatakBroda--;
            return true;
        }
        return false;
    }
    
    /** dodaje lokaciju za broj*/
    public void dodajLokaciju(int i){
        lokacija.add(i);
        ostatakBroda++;
    }
    
    /**da li je broj unisten */
    public boolean unisten(){
        return ostatakBroda==0;
    }
    
    /**ispis lokacije broda */
    @Override
    public String toString(){
        StringBuffer s=new StringBuffer("[");
        for(int i=0;i<lokacija.size();i++){
            s.append(lokacija.get(i)+" ");
        }
        s.append("]"+ " Length: "+ostatakBroda);
        return s.toString();
    }
}
