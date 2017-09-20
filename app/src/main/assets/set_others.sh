#!/system/bin/sh
# Feature XML Edit
# created by ~clumsy~
# Usage:
# set_xml.sh <file_name>  <feature_name> <value_to_change_to>
# Note: If there is any spaces in a argument, capture them in quotations e.g. 'Some String'
# Example_1: set_xml.sh other.xml CscFeature_IMS_EnableVoLTE False
# Example_2: set_xml.sh other.xml CscFeature_Common_AutoConfigurationType 'NO_DFLT, SIMBASED_OMC'
 
file=$1
feature=$2
value=$3
 
lineNumber=0
lineNumber=`sed -n "/<${feature}>.*<\/${feature}>/=" $file`
 
if [ $lineNumber > 0 ] ; then
    echo "Found feature $feature in line $lineNumber and changing it to ${value}"
    sed -i "${lineNumber} c<${feature}>${value}<\/${feature}>" $file
else
    echo "Adding feature $feature to the feature set"
    sed -i "/<\/FeatureSet>/i <${feature}>${value}<\/${feature}>" $file
fi