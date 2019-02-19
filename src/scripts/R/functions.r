# palisades dot lakes at gmail dot com
# 2018-10-09
#-----------------------------------------------------------------
# Load the necessary add-on packages, downloading and installing
# (in the user's R_LIBS_USER folder) if necessary.
load.packages <- function () {
  user.libs <- Sys.getenv('R_LIBS_USER')
  dir.create(user.libs,showWarnings=FALSE,recursive=TRUE)
  repos <- c('http://cran.fhcrc.org')
  packages <- 
    c(# lattice needed for the trellis device, could probably
      # rewrite dev.on to eliminate that
      'lattice',  
      'latticeExtra',
      'ggplot2',
      'RColorBrewer')
  for (package in packages) {
    found <- eval(call('require',package,quietly=TRUE))
    if (! found) { 
      install.packages(c(package),user.libs,repos=repos,deps=true)
      eval(call('library',package)) } } }
#-----------------------------------------------------------------
load.packages()
#-----------------------------------------------------------------
# see
# https://stackoverflow.com/questions/27788968/how-would-one-check-the-system-memory-available-using-r-on-a-windows-machine
free.ram <- function () {
  if(Sys.info()[["sysname"]] == "Windows"){
    x <- system2("wmic", args =  "OS get FreePhysicalMemory /Value", stdout = TRUE)
    x <- x[grepl("FreePhysicalMemory", x)]
    x <- gsub("FreePhysicalMemory=", "", x, fixed = TRUE)
    x <- gsub("\r", "", x, fixed = TRUE)
    floor(as.integer(x) / (1024 * 1024))
  } else {
    stop("Only supported on Windows OS")
  }
}
#-----------------------------------------------------------------
read.tsv <- function (file) {
  read.table(
    sep='\t',
    file=file,
    header=TRUE,
    stringsAsFactors=FALSE) }
#-----------------------------------------------------------------
# plots
#-----------------------------------------------------------------
my.theme <- function () {
  n <- 7
  text.color <- 'black'
  dark <- brewer.pal(n,'Dark2')
  pastel <- brewer.pal(n,'Pastel2')
  list(
    background=list(col='transparent'),
    #fontsize='16',
    axis.line=list(col='gray'),
    axis.text=list(col=text.color,cex=4),
    box.rectangle=list(col='darkgreen'),
    box.umbrella=list(col='darkgreen'),
    dot.line=list(col='#e8e8e8'),
    dot.symbol=list(col='darkgreen'),
    par.xlab.text=list(col=text.color,cex=4),
    par.xlab.text=list(col=text.color,cex=4),
    par.ylab.text=list(col=text.color,cex=1.5),
    par.main.text=list(col=text.color,cex=2),
    par.sub.text=list(col=text.color,cex=1.5),
    plot.line=list(col='darkgreen'),
    plot.symbol=list(col='darkgreen'),
    plot.polygon=list(col='darkgreen'),
    reference.line=list(col='#e8e8e8'),
    regions=list(col=colorRampPalette(rev(brewer.pal(11,'RdYlGn')))(100)),
    shade.colors=list(palette=colorRampPalette(rev(brewer.pal(11,'RdYlGn')))),
    strip.border=list(col='gray'),
    strip.shingle=list(col=dark),
    strip.background=list(col='gray'),
    superpose.line=list(col=dark,lty=1:n,lwd=1),
    superpose.polygon=list(col=dark,border=rep('#DDDDDD44',n),alpha=rep(0.3,n)),
    superpose.symbol=list(pch=c(1,3,6,0,5,16,17),cex=rep(1, n),col=dark,fontface='bold')) }
#lattice.options(default.theme=my.theme)
#lattice.options(lattice.theme=my.theme)
#-----------------------------------------------------------------
#theme_set(theme_bw())
#-----------------------------------------------------------------
# Open a png graphics device.
# aspect ratio is height/width

dev.on <- function (
  filename,
  aspect=(1050/1400),
  width=1400,
  theme=my.theme) {
  
  # make sure the folder is there
  dir.create(dirname(filename),showWarnings=FALSE,recursive=TRUE)
  
  # often the graphics device is stuck from the last failed run
  options(show.error.messages=FALSE)
  options(warn=-1)
  try( dev.off() )
  options(warn=0)
  options(show.error.messages=TRUE)
  
  w <- width
  h <- aspect*w
  
  # The png device doesn't work under my R installation on linux.
  # The bitmap device doesn't work on my windowslaptop.
  # At least they both produce png files.
  plotF <- paste(filename,'png',sep='.')
  print(plotF)
  if ('windows'==.Platform$OS.type) {
    trellis.device(device='png',theme=theme,
      filename=plotF,width=w,height=h) }
  else { 
    trellis.device(device='bitmap',theme=theme,
      file=plotF,width=w,height=h,theme=theme) } }
#-----------------------------------------------------------------
# consistent colors accross plots
functionals <- c(
  'testf',
  'argmin',
  'valueKnot',
  'slopeKnot',
  'ConstantFunctional',
  'AffineFunctional',
  'QuadraticLagrange',
  'QuadraticMonomial',
  'QuadraticMonomialShifted',
  'QuadraticMonomialStandardized', 
  'QuadraticNewton',
  'CubicHermite', 
  'CubicLagrange', 
  'CubicMonomial', 
  'CubicNewton')
functional.colors <- c(
  '#88888850',
  '#000000FF',
  '#0000FF50',
  '#FF000050',
  '#386cb050',
  '#1b9e7750',
  '#66a61e50',
  '#66a61e50',
  '#a6761d50',
  '#e41a1cFF',
  '#e41a1cFF',
  '#1c1ae4FF',
  '#75707050',
  '#75707050',
  '#6a3d9a50')
#-----------------------------------------------------------------
# plots
#-----------------------------------------------------------------
#-----------------------------------------------------------------

