data <- read.table("cpu-620-clients-st-false.txt")

response <- read.table("delay-620-clients-st-false.txt")


data$V1 <- ( data$V1 - data$V1[1]) / 1000
data$V2 <- data$V2 * 100

response$V2 <- ( response$V2 - response$V2[1]) / 1000
response$V3 <- response$V3 / 1000

out2 <- response$V2[ seq_along(response$V2) %% 100 == 0 ]
out3 <- response$V3[ seq_along(response$V3) %% 100 == 0 ]

postscript(file="cpu-load-620-mt.eps")

par(mfrow=c(2,1))
par(mar=c(1, 4, 0.5, 0.5))
plot(data, type="l", xaxt="n", ylab="CPU load (%)", cex.lab=1.4,  xlim=c(0,250))
par(mar=c(4, 4, 0, 0.5))

plot(out2, out3, type="l", ylab="Response time (s)", xlab="Seconds since start of test", cex.lab=1.4, ylim=c(0, 1),  xlim=c(0,250))


dev.off()
