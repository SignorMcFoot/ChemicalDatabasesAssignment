package DataAcquisition;

import chemaxon.marvin.calculations.*;
import chemaxon.marvin.plugin.PluginException;
import chemaxon.struc.Molecule;

//The object with logic from ChemAxon.calculation pllugins
public class MoleculeTuple {
    private String name;
    private double mass;
    private double logP;
    private double logD;
    private int hDonors;
    private int hAcceptors;
    private int ringCount;
    private int rotatableBondCount;
    private double PSA;
    private int fusedAromaticCount;

    public MoleculeTuple(Molecule molecule) {
        this.name = molecule.getName();
        this.mass = molecule.getMass();
        try {
            this.logP = calcLogP(molecule);
            this.logD = calcLogD(molecule);
            this.hDonors = calcHydroDonorAcceptor(molecule)[0];
            this.hAcceptors = calcHydroDonorAcceptor(molecule)[1];
            this.ringCount = calcTopology(molecule)[0];
            this.rotatableBondCount = calcTopology(molecule)[1];
            this.fusedAromaticCount = calcTopology(molecule)[2];
            this.PSA = calcPSA(molecule);
        } catch (PluginException e) {
            e.printStackTrace();
        }
    }
    //Method for parsing the data into a format valid for the entry to the database
    public String entryData() {
        return ("\"" + name + "\"" + "," + mass + "," + logP + "," + logD + "," + hDonors + "," + hAcceptors + "," + ringCount + "," + rotatableBondCount + "," + PSA + "," + fusedAromaticCount);
    }
    //Below are methods using the plugins to calculate the required values.
    //The precisions as well as other additional values required to run the plugins were taken from the official documentation

    private double calcLogP(Molecule m) throws PluginException {
        logPPlugin pl = new logPPlugin();
        pl.setMolecule(m);
        pl.setDoublePrecision(2);
        pl.run();
        return pl.getlogPTrue();

    }

    private double calcLogD(Molecule m) throws PluginException {
        logDPlugin pl = new logDPlugin();
        pl.setMolecule(m);
        pl.setpH(7.4);
        pl.setDoublePrecision(2);
        pl.run();
        return pl.getlogD();

    }

    private int[] calcHydroDonorAcceptor(Molecule m) throws PluginException {
        HBDAPlugin pl = new HBDAPlugin();
        pl.setDoublePrecision(2);
        pl.setpHLower(2.0);
        pl.setpHUpper(12.0);
        pl.setpHStep(2.0);
        pl.setMolecule(m);
        pl.run();
        return new int[]{pl.getAcceptorAtomCount(), pl.getDonorAtomCount()};
    }

    private int[] calcTopology(Molecule m) throws PluginException {
        TopologyAnalyserPlugin tp = new TopologyAnalyserPlugin();
        tp.setMolecule(m);
        tp.run();
        return new int[]{tp.getRingCount(), tp.getRotatableBondCount(), tp.getFusedAromaticRingCount()};
    }

    private double calcPSA(Molecule m) throws PluginException {
        TPSAPlugin psa = new TPSAPlugin();
        psa.setMolecule(m);
        psa.run();
        psa.setpH(7.4);
        return psa.getSurfaceArea();
    }
}
