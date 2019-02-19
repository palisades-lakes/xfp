# palisades dot lakes at gmail dot com
# 2018-10-09
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/xfp')) {
  setwd('e:/porta/projects/xfp')
} else {
  setwd('c:/porta/projects/xfp')
}
source('src/scripts/R/functions.r')
#-----------------------------------------------------------------
data.folder <- file.path('data','interpolate','macro')
plot.folder <- file.path('plots','interpolate','macro')
data.files <- list.files(
  path=data.folder,pattern='*.macro.tsv',full.names=FALSE) 
#-----------------------------------------------------------------
for (data.file in data.files) {
  data <- read.tsv(file.path(data.folder,data.file))
  print(nrow(data))
  data$functional <- factor(x=data$functional,levels=functionals)
  knots <- 
    data[(data$functional=='argmin') | 
        (data$functional=='valueKnot') | 
        (data$functional=='slopeKnot') ,]
  curves <- 
    data[(data$functional!='argmin') & 
        (data$functional!='valueKnot') & 
        (data$functional!='slopeKnot') ,]
  dev.on(
    file=file.path(plot.folder,data.file),
    aspect=1016/1856,
    width=1856)
  plot <- ggplot(curves, aes(x=x, y=y, color=functional)) +
    geom_point(data=knots,size=4.0) + 
    geom_line(size=1.0) + 
  scale_color_manual(values=(functional.colors),drop=FALSE) +
  theme(text=element_text(size=24))#+
  #ggtitle("lower is better")
  print(plot)
  dev.off() }
#-----------------------------------------------------------------
