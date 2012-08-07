TEXFILE=pb

all: pdf

dvi:
	latex $(TEXFILE).tex
	bibtex $(TEXFILE).aux
	latex $(TEXFILE).tex
	latex $(TEXFILE).tex

ps: dvi
	dvips $(TEXFILE).dvi

pdf: ps
	ps2pdf $(TEXFILE).ps

clean:
	rm *.aux *.bbl *.blg *.dvi *.log *.ps *.pdf
