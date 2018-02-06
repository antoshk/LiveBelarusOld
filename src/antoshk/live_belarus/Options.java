/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antoshk.live_belarus;

/**
 *
 * @author User
 */
public class Options {
    static final Options appOptions = new Options();

    private boolean hasWindowForm = false;
    private javax.swing.JTextArea outputTextArea, mainTextArea, tagTextArea;
    private javax.swing.JProgressBar progressBar;

    /**
     * @return the hasWindowForm
     */
    public boolean hasWindowForm() {
        return hasWindowForm;
    }

    /**
     * @param hasWindowForm the hasWindowForm to set
     */
    public void setHasWindowForm(boolean hasWindowForm) {
        this.hasWindowForm = hasWindowForm;
    }

    /**
     * @return the outputTextArea
     */
    public javax.swing.JTextArea getOutputTextArea() {
        return outputTextArea;
    }

    /**
     * @param outputTextArea the outputTextArea to set
     */
    public void setOutputTextArea(javax.swing.JTextArea outputTextArea) {
        this.outputTextArea = outputTextArea;
    }

    /**
     * @return the mainTextArea
     */
    public javax.swing.JTextArea getMainTextArea() {
        return mainTextArea;
    }

    /**
     * @param mainTextArea the mainTextArea to set
     */
    public void setMainTextArea(javax.swing.JTextArea mainTextArea) {
        this.mainTextArea = mainTextArea;
    }

    /**
     * @return the tagTextArea
     */
    public javax.swing.JTextArea getTagTextArea() {
        return tagTextArea;
    }

    /**
     * @param tagTextArea the tagTextArea to set
     */
    public void setTagTextArea(javax.swing.JTextArea tagTextArea) {
        this.tagTextArea = tagTextArea;
    }

    /**
     * @return the progressBar
     */
    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * @param progressBar the progressBar to set
     */
    public void setProgressBar(javax.swing.JProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
