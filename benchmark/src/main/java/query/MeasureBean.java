package query;

/**
 * Created by Anila Sahar Butt.
 */
public class MeasureBean {

    String property;
    String propertyLabel;
    String value;
    String valueLabel;

    /******************** Constructor *************************/
    public MeasureBean(){
        property = "";
        propertyLabel="";
        value = "";
        valueLabel="";
    }

    /******************** Setter Function *************************/

    public void setProperty(String prop) {
        property = prop; }

    public void setPropertyLabel(String proplabel) {
        propertyLabel = proplabel; }

    public void setValue(String val) {
        value = val; }

    public void setValueLabel(String valLab) {
        valueLabel = valLab; }


    /******************** Getter Function *************************/

    public String getProperty() {
        return this.property; }

    public String getPropertyLabel() {
        return this.propertyLabel; }

    public String getValue() {
        return this.value; }

    public String getValueLabel() {
        return this.valueLabel; }

}
