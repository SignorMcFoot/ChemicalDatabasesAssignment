package DataAcquisition;
import java.io.*;
import java.util.ArrayList;
import chemaxon.struc.Molecule;
import chemaxon.formats.MolImporter;

public class DataInput {

    //The main method for reading the provided .sdf file into an ArrayList of the object Molecule
    public ArrayList<Molecule> inputFile(String filename) throws IOException {
        ArrayList<Molecule> listFromFile = new ArrayList<>();
        MolImporter mi = new MolImporter(filename);
        Molecule m = mi.read();
        while (m != null) {
            m = mi.read();
            listFromFile.add(m);
        }
        mi.close();
        return listFromFile;
    }
    //Method for parsing the values from the provided ArrayList into a primitive array of Strings based on the calculated values
    public String[] databaseInput(ArrayList<Molecule> listOfMolecules){
        MoleculeTuple mt;
        String[] array = new String[listOfMolecules.size()];
        for(int k = 0; k < listOfMolecules.size()-1; k++) {
            mt = new MoleculeTuple(listOfMolecules.get(k));
            array[k] = mt.entryData();
        }
        return array;
    }
}
