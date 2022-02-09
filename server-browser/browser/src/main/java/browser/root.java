package browser;

import java.util.Vector;

public class root {
    public String annoDiInizio;
    public String classe;
    public int numeroFinestre;
    public String specializzazione;
    public aula aula=new aula();
    Vector<element> studenti=new Vector<>();
    
    public Vector<element> getStudenti() {
        return studenti;
    }
    public void setStudenti(Vector<element> studenti) {
        this.studenti = studenti;
    }
    public String getAnnoDiInizio() {
        return annoDiInizio;
    }
    public void setAnnoDiInizio(String annoDiInizio) {
        this.annoDiInizio = annoDiInizio;
    }
    public String getClasse() {
        return classe;
    }
    public void setClasse(String classe) {
        this.classe = classe;
    }
    public int getNumeroFinestre() {
        return numeroFinestre;
    }
    public void setNumeroFinestre(int numeroFinestre) {
        this.numeroFinestre = numeroFinestre;
    }
    public String getSpecializzazione() {
        return specializzazione;
    }
    public void setSpecializzazione(String specializzazione) {
        this.specializzazione = specializzazione;
    }
    public aula getAula() {
        return aula;
    }
    public void setAula(aula aula) {
        this.aula = aula;
    }
    
    

    




    
}
