# Get 10 dumps with intervals for single-threaded and multithreaded
max <- 256
threads <- seq(2, max, 2)

names <- c()
filenames <- c()
values <- c()
percentiles <- c()

for(i in 1:length(threads)){
  names <- cbind(names, paste("data", threads[i], sep=""))
  filenames <- cbind(filenames, paste("stable-delay-700-clients-", threads[i],"-threads.txt",sep=""))
}

for(i in 1:(length(threads))){
  # Assign delay column of files to values array
  assign(names[i], read.table(filenames[i])$V3)
  percentiles <- rbind(percentiles,quantile(get(names[i]),c(0.05,0.50,0.95)))
}

postscript(file="lineplot-threads.eps")
#boxplot(values, xlim=c(1, max),at=threads,  ylim=c(100,2800),range=3, xaxt="n", ylab="Delay per scheduled run (ms)", xlab="Number of threads in threadpool", cex.lab=1.4, cex.axis=1.3, labels=threads)
plot(threads,percentiles[,2],type="l", ylim=c(0,2800),ylab="Delay per scheduled run (ms)", xlab="Number of threads in threadpool",xlim=c(2, max))
#lines(threads,percentiles[,2])
#lines(threads,percentiles[,3])
polygon(x=c(threads,rev(threads)),y=c(percentiles[,3],rev(percentiles[,1])), col=rgb(220/255, 220/255, 220/255), lty=2)
lines(threads,percentiles[,2])
#axis(1, at=threads, labels=threads, cex.lab=1.4, cex.axis=1.3)
dev.off()
