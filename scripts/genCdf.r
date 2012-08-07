stdata <- read.table("stable-delay-400-clients-st-false.txt")$V3
mtdata <- read.table("stable-delay-400-clients-st-true.txt")$V3

stcdf <- ecdf(stdata)
mtcdf <- ecdf(mtdata)

max <- max(stdata, mtdata)

postscript("cdf-400.eps")
#svg("cdf-200.svg")

plot(stcdf, verticals = TRUE, do.p = FALSE, pch=NA, main=NULL, xlab="Delay for each scheduled run (ms)", ylab="CDF", col="red", xlim=c(0, 5000),cex.axis=2.0,cex.lab=2, cex=2,mgp=c(2.5, 1, 0) )
lines(mtcdf, verticals = TRUE, do.p = FALSE, pch=NA, col="blue")

legend("bottomright",0.5, c("Multithreaded", "Single threaded"), col=c("red", "blue"),lty=c(1, 1),inset=c(0.01,0.05),cex=2)

dev.off()
