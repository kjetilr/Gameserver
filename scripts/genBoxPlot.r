# Get 10 dumps with intervals for single-threaded and multithreaded
clients <- seq(40, 800, 40)

#names <- c()
filenames <- c()
values <- c()

for(i in 1:length(clients)){
  names <- cbind(names, paste("mt", clients[i], sep=""))
  filenames <- cbind(filenames, paste("stable-delay-", clients[i], "-clients-st-false.txt",sep=""))
}

for(i in 1:(length(clients))){
  # Assign delay column of files to values array
  assign(names[i], read.table(filenames[i])$V3)
  values <- cbind(values, get(names[i]))
}

postscript(file="boxplot.eps")

boxplot(values, ylim=c(100,2800), xlab="Number of concurrent clients",xaxt="n",
        ylab="Delay per scheduled run (ms)", cex.lab=1.5, cex.axis=1.3,range=0)
axis(1, at=seq(1:20), labels=clients, cex.lab=1.0, cex.axis=1.0)

abline(h=100, lty=2)


dev.off()

