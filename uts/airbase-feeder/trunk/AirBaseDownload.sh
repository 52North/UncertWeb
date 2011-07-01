#!/bin/sh

BASEURL='http://www.eea.europa.eu/data-and-maps/data/airbase-the-european-air-quality-database-2'
SUFFIX='at_download/file'



wget -O AirBase_AD_v4.zip $BASEURL/andorra/andorra/$SUFFIX
wget -O AirBase_AT_v4.zip $BASEURL/austria/austria/$SUFFIX
wget -O AirBase_BE_v4.zip $BASEURL/belgium/belgium/$SUFFIX
wget -O AirBase_BA_v4.zip $BASEURL/bosnia-and-herzegovina/bosnia-and-herzegovina/$SUFFIX
wget -O AirBase_BG_v4.zip $BASEURL/bulgaria/bulgaria/$SUFFIX

#wget -O AirBase_HR_v4.zip $BASEURL/croatia/croatia/$SUFFIX
#wget -O AirBase_CY_v4.zip $BASEURL/cyprus/cyprus/$SUFFIX
#wget -O AirBase_CZ_v4.zip $BASEURL/czech-republic/czech-republic/$SUFFIX
#wget -O AirBase_DK_v4.zip $BASEURL/denmark/denmark/$SUFFIX
#wget -O AirBase_EE_v4.zip $BASEURL/estonia/estonia/$SUFFIX
#wget -O AirBase_FI_v4.zip $BASEURL/finland/finland/$SUFFIX
#wget -O AirBase_FR_v4.zip $BASEURL/france/france/$SUFFIX
#wget -O AirBase_DE_v4.zip $BASEURL/germany/germany/$SUFFIX
#wget -O AirBase_GR_v4.zip $BASEURL/greece/greece/$SUFFIX
#wget -O AirBase_HU_v4.zip $BASEURL/hungary/hungary/$SUFFIX
#wget -O AirBase_IS_v4.zip $BASEURL/iceland/iceland/$SUFFIX
#wget -O AirBase_IE_v4.zip $BASEURL/ireland/ireland/$SUFFIX
#wget -O AirBase_IT_v4.zip $BASEURL/italy/italy/$SUFFIX
#wget -O AirBase_LV_v4.zip $BASEURL/latvia/latvia/$SUFFIX
#wget -O AirBase_LI_v4.zip $BASEURL/liechtenstein/liechtenstein/$SUFFIX
#wget -O AirBase_LT_v4.zip $BASEURL/lithuania/lithuania/$SUFFIX
#wget -O AirBase_LU_v4.zip $BASEURL/luxembourg/luxembourg/$SUFFIX
#wget -O AirBase_MK_v4.zip $BASEURL/macedonia-former-yugoslav-rep/macedonia-former-yugoslav-rep/$SUFFIX
#wget -O AirBase_MT_v4.zip $BASEURL/malta/malta/$SUFFIX
#wget -O AirBase_NL_v4.zip $BASEURL/netherlands/netherlands/$SUFFIX
#wget -O AirBase_NO_v4.zip $BASEURL/norway/norway/$SUFFIX
#wget -O AirBase_PL_v4.zip $BASEURL/poland/poland/$SUFFIX
#wget -O AirBase_PT_v4.zip $BASEURL/portugal/portugal/$SUFFIX
#wget -O AirBase_RO_v4.zip $BASEURL/romania/romania/$SUFFIX
#wget -O AirBase_RS_v4.zip $BASEURL/serbia/serbia/$SUFFIX
#wget -O AirBase_SK_v4.zip $BASEURL/slovakia/slovakia/$SUFFIX
#wget -O AirBase_SI_v4.zip $BASEURL/slovenia/slovenia/$SUFFIX
#wget -O AirBase_ES_v4.zip $BASEURL/spain/spain/$SUFFIX
#wget -O AirBase_SE_v4.zip $BASEURL/sweden/sweden/$SUFFIX
#wget -O AirBase_CH_v4.zip $BASEURL/switzerlands/switzerlands/$SUFFIX
#wget -O AirBase_TR_v4.zip $BASEURL/turkey/turkey/$SUFFIX
#wget -O AirBase_GB_v4.zip $BASEURL/united-kingdom/united-kingdom/$SUFFIX 
