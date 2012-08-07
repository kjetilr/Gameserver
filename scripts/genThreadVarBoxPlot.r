# Get 10 dumps with intervals for single-threaded and multithreaded
max <- 64
threads <- seq(2, max, 2)

names <- c()
filenames <- c()
values <- c()

for(i in 1:length(threads)){
  names <- cbind(names, paste("data", threads[i], sep=""))
  filenames <- cbind(filenames, paste("stable-delay-700-clients-", threads[i],"-threads.txt",sep=""))
}
#stable-delay-700-clients-128-threads.txt
for(i in 1:(length(threads))){
  # Assign delay column of files to values array
  assign(names[i], read.table(filenames[i])$V3)
  values <- cbind(values, get(names[i]))
}

postscript(file="boxplot-threads.eps")
#pdf(file="boxplot-threads.pdf")
#jpeg(file="boxplot-threads.jpg", width = 1500, height = 1000, units = "px", quality = 75)

#svg(file="boxplot-threads.svg")
boxplot(values, xlim=c(1, max),at=threads,  ylim=c(100,2800),range=3, ylab="Delay per scheduled run (ms)", xlab="Number of threads in threadpool", cex.lab=1.4, cex.axis=1.3, labels=threads)
#axis(1, at=threads, labels=threads, cex.lab=1.4, cex.axis=1.3)

dev.off()
