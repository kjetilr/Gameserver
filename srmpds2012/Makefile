# Note:
# - set BIBHOME to the correct path
# - inkscape is required for figures to be generated

TEXFILE = srmpds2012
FILES = srmpds2012.tex abstract.tex intro.tex concept.tex conclusion.tex implementation.tex evaluation.tex
FIGURES := $(patsubst %.svg,%.eps,$(wildcard FIG/*.svg))

#BIBHOME ?= /home/apetlund/svn/bibfile
vpath %.bib ${BIBHOME}

#BIBFILES = div.bib mmog.bib macros.bib own.bib tools.bib games.bib transport-protocols.bib rfc.bib thin-stream-applications.bib

all:    $(TEXFILE).dvi

ps:     $(TEXFILE).ps

pdf:    $(TEXFILE).pdf

$(TEXFILE).aux: $(FILES) 
	latex $(FILES)

$(TEXFILE).bbl: $(TEXFILE).aux all.bib
	bibtex $(TEXFILE)

%.dvi: %.bbl
	latex $*
	latex $*

%.ps: %.dvi
	dvips -t letter -Ppdf -j0 -G0 $<

%.pdf: %.ps
	ps2pdf14 -dPDFSETTINGS=/prepress -dEmbedAllFonts=true $<

%.eps: %.svg
	inkscape -z -T -E $@ $<

softlink:
	ln -fs ../SUBMIT/FIG .


all.bib: ${BIBFILES}
	cat $^ > $@

clean: 
#	rm -f *.log *.aux  *.blg *.dvi *.bak
	rm -f *.log *.aux *.bbl *.blg *.dvi *.bak
	rm -f *.backup.fm
	rm -f *.ps *.pdf
	rm -f *~

realclean: clean
	rm all.bib
