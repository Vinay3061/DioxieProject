library(plumber)
library(randomForest)
library(readr)
library(readxl)
library(dplyr)


#load the model-DT-pp
super_model_DT <- readRDS("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Super_model_DT_PP_Feb23.rds")

#load the model-LR-pp
super_model_LR <- readRDS("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Super_model_LR_PP_Feb23.rds")

#load the model-DT-tvc
super_model_DT1 <- readRDS("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Super_model_DT_TVC_Feb23.rds")

#load the model-LR-tvc
super_model_LR1 <- readRDS("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Super_model_LR_TVC_Feb23.rds")

PP_seg=read_csv("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Scoring_Data_DT_PP_Feb23.csv")

TVC_seg=read_csv("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Scoring_Data_DT_TVC_Feb23.csv")

###

# Read the Excel file into a data frame and remove duplicates
input_data <- read_excel("C:/Users/Administrator/Downloads/Price Reco-20230412T042841Z-001/Price Reco/Unit cars DB.xlsx") %>%
  distinct(Make, Model, Grade, Suffix, .keep_all = TRUE)

# Create lookup table
lookup_table <- input_data %>% 
  select(Make, Model, Grade, Suffix) %>% 
  distinct()

# Validate make and model
validate_make_model_grade_suffix <- function(Make, Model, Grade, Suffix) {
  make_model_grade_suffix <- paste(Make, Model, Grade, Suffix, sep = "_")
  lookup_make_model_grade_suffix <- paste(lookup_table$Make, lookup_table$Model, lookup_table$Grade, lookup_table$Suffix, sep = "_")
  return(make_model_grade_suffix %in% lookup_make_model_grade_suffix)
}


# collect user input to make the predictions
#* @param Make
#* @param Model
#* @param Grade
#* @param Suffix
#* @param MfgYear
#* @param Odometer.Reading
#* @param No.Of.Ownership
#* @param Color
#* @param Insurance.Type
#* @param Source.Type
#* @param Vehicle.Classification
#* @param Fuel.Type
#* @param Quality.Level
#* @param New.Car.Trade.in
#* @get /Price-Reco

predictions<-function(Make,Model,Grade,Suffix,MfgYear,Odometer.Reading,
                      No.Of.Ownership,Color,Insurance.Type,Source.Type,
                      Vehicle.Classification,Fuel.Type,Quality.Level,New.Car.Trade.in){
  
  
  
  df=data.frame(matrix(ncol =14,nrow = 1))
  x=c("Make","Model","Grade","Suffix","MfgYear","Odometer.Reading","No.Of.Ownership","Color","Insurance.Type","Source.Type","Vehicle.Classification","Fuel.Type","Quality.Level","New.Car.Trade.in")
  colnames(df)=x
  
  #Add intput info to dataframe df
  df$Make=Make
  df$Model=Model
  df$Grade=Grade
  df$Suffix=Suffix
  df$`MfgYear`=as.numeric(MfgYear)
  df$`Odometer.Reading`=as.numeric(Odometer.Reading)
  df$`No.Of.Ownership`=as.numeric(No.Of.Ownership)
  df$`Color`=Color 
  df$`Insurance.Type`=Insurance.Type 
  df$`Source.Type`=Source.Type
  df$`Vehicle.Classification`=Vehicle.Classification
  df$`Fuel.Type`=Fuel.Type
  df$`Quality.Level`=as.numeric(Quality.Level)
  df$`New.Car.Trade.in`=New.Car.Trade.in
  df[sapply(df, is.character)] <- lapply(df[sapply(df, is.character)], as.factor)
  
  # Validate input make, model, grade, suffix
  if (!validate_make_model_grade_suffix(df$Make, df$Model, df$Grade, df$Suffix)) {
    return("Unfortunately, we were unable to locate any information for the car you specified. Please review your inputs and try again.")
  }
  
  
  ##Predicted DT_PP
  Predicted_price_DT_PP =predict(super_model_DT,df)
  Pred_DT_PP=round(as.numeric(Predicted_price_DT_PP),2)
  
  ##Predicted DT_TVC
  Predicted_price_DT_TVC =predict(super_model_DT1,df)
  Pred_DT_TVC=round(as.numeric(Predicted_price_DT_TVC),2)
  
  ##Predicted LR_PP
  Predicted_price_LR_PP =predict(super_model_LR,df)
  Pred_LR_PP=round(as.numeric(Predicted_price_LR_PP),2)
  typeof(Pred_LR_PP)
  
  ##Predicted LR_TVC
  Predicted_price_LR_TVC =predict(super_model_LR1,df)
  Pred_LR_TVC=round(as.numeric(Predicted_price_LR_TVC),2)
  
  
  Price_Group_PP= paste("PG-",round(Pred_DT_PP/100000,2),"L",sep="")
  
  Price_Group_TVC= paste("PG-",round(Pred_DT_TVC/100000,2),"L",sep="")
  
  PP_seg_cars=PP_seg[PP_seg$Make == Make & PP_seg$Model == Model & PP_seg$Grade == Grade & PP_seg$Suffix == Suffix & PP_seg$`Fuel.Type` == Fuel.Type & PP_seg$SegID == Price_Group_PP,]
  
  
  nCars_PP<-nrow(PP_seg_cars)
  Q1_PP=quantile(PP_seg_cars$`Total.Purchase.Price`, 0.25)
  Q2_PP=quantile(PP_seg_cars$`Total.Purchase.Price`, 0.50)
  Q3_PP=quantile(PP_seg_cars$`Total.Purchase.Price`, 0.75)
  
  
  TVC_seg_cars=TVC_seg[TVC_seg$Make == Make & TVC_seg$Model == Model & TVC_seg$Grade == Grade & TVC_seg$Suffix == Suffix & TVC_seg$`Fuel.Type` == Fuel.Type & TVC_seg$SegID == Price_Group_TVC,]
  
  
  nCars_TVC<-nrow(TVC_seg_cars)
  Q1_TVC=quantile(TVC_seg_cars$`Total.Vehicle.Cost`, 0.25)
  Q2_TVC=quantile(TVC_seg_cars$`Total.Vehicle.Cost`, 0.50)
  Q3_TVC=quantile(TVC_seg_cars$`Total.Vehicle.Cost`, 0.75)
  
  Q1=min(Q1_PP,Q1_TVC)   ## Q1(min) to be displayed in the frontend
  Q2=mean(Q2_PP,Q2_TVC)
  Q3=max(Q3_PP,Q3_TVC)  ## Q3(max)to be displayed in the frontend  
  
  res1=list()
  res1$Pred_LR_PP<-Pred_LR_PP
  res1$Pred_MLP_PP<-"Dummy"
  res1$Pred_DT_PP<-Pred_DT_PP
  res1$Price_Group_PP<-Price_Group_PP
  res1$nCars_PP<-nCars_PP
  res1$Q1_PP<-Q1_PP
  res1$Q2_PP<-Q2_PP
  res1$Q3_PP<-Q3_PP
  
  #tvc
  res1$Pred_LR_TVC<-Pred_LR_TVC
  res1$Pred_MLP_TVC<-"Dummy"
  res1$Pred_DT_TVC<-Pred_DT_TVC
  res1$Price_Group_TVC<-Price_Group_TVC
  res1$nCars_TVC<-nCars_TVC
  res1$Q1_TVC<-Q1_TVC
  res1$Q2_TVC<-Q2_TVC
  res1$Q3_TVC<-Q3_TVC
  
  ## Q1 & Q3 TO BE DISPLAYED IN THE FRONTEND
  res1$Q1<-Q1
  res1$Q2<-Q2
  res1$Q3<-Q3
  return(res1)
}




