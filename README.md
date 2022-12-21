# CSFD VOD Searcher

### Wtf?

- Toto je len take male udelatko, aby som mohol vyhladavat na CSFD podla VOD (streamingovej sluzby)
- CSFD nema API, takze to scrapuje HTML
- Casom sa to urcite rozbije, pretoze to pouziva CSS selektory na ziskavanie dat z HTML elementov

### Building

- `./gradlew clean build installDist`

### Using

```% ./build/install/csfd-vod-searcher/bin/csfd-vod-searcher --help```

or, better:

```
% alias csfd=./build/install/csfd-vod-searcher/bin/csfd-vod-searcher
% csfd --help

Usage: csfd [OPTIONS] COMMAND [ARGS]...

  CSFD VOD searcher

Options:
  -h, --help  Show this message and exit

Commands:
  config  Configure CSFD credentials
  search  Search movies by VOD availability
```

### Samples

- Konfiguracia (nie je nutna, ale je potrebna aby system rozoznal, ktore filmy som uz videl):
```
% csfd config moj_login moje_heslo

Logged into CSFD.cz as moj_login
SUCCESSFUL
```

- Zoznam podporovanych VOD:
```
% csfd search ???

Usage: csfd search [OPTIONS] VOD

Error: Invalid value for "VOD": invalid choice: src. (choose from Netflix, HBOMax, AppleTV+, AmazonPrime, GooglePlay, iTunes, Disney+)
```

- Rychle (paralelne) hladanie podla VOD
```
% csfd search HBOMax

Using CSFD.cz without login
Smrtonosná zbraň (88%)
	http://csfd.cz/film/2438-smrtonosna-zbran/
Pán prstenů: Společenstvo Prstenu (91%)
	http://csfd.cz/film/4711-pan-prstenu-spolecenstvo-prstenu/
Osvícení (88%)
	http://csfd.cz/film/5407-osviceni/
... 
```

- Rychle (paralelne) hladanie podla VOD so strankovanim
```
% csfd search HBOMax --page=2

Using CSFD.cz without login
Jak vycvičit draka (86%)
	http://csfd.cz/film/234768-jak-vycvicit-draka/
Kolja (86%)
	http://csfd.cz/film/8805-kolja/
Olověná vesta (86%)
	http://csfd.cz/film/5401-olovena-vesta/
...
```

- Pomale (sekvencne) hladanie s utriedenymi vysledkami (vypisuje progress kvoli blokujucej operacii)
```
% csfd search HBOMax --sorted

Using CSFD.cz without login
10 % done!
20 % done!
30 % done!
40 % done!
50 % done!
60 % done!
70 % done!
80 % done!
90 % done!
Pelíšky (91%)
	http://csfd.cz/film/4570-pelisky/
Pán prstenů: Společenstvo Prstenu (91%)
	http://csfd.cz/film/4711-pan-prstenu-spolecenstvo-prstenu/
Pán prstenů: Návrat krále (91%)
	http://csfd.cz/film/4712-pan-prstenu-navrat-krale/
Temný rytíř (90%)
	http://csfd.cz/film/223734-temny-rytir/
Gran Torino (90%)
	http://csfd.cz/film/240479-gran-torino/
...
```

- Pomale (sekvencne) hladanie, ktore odfiltruje uz videne filmy. Toto funguje na zaklade (ne)pritomnosti hodnotenia pouzivatela. Nutny `csfd config` pred pouzitim. Pomale je to preto, lebo CSFD by blokovalo paralelene dotazy s rovnakymi cookies.
```
% csfd search HBOMax --unseen-only

Logged into CSFD.cz as moj_login
10 % done!
20 % done!
30 % done!
Zachraňte vojína Ryana (89%)
	http://csfd.cz/film/8652-zachrante-vojina-ryana/
40 % done!
50 % done!
60 % done!
Smrtonosná zbraň (88%)
	http://csfd.cz/film/2438-smrtonosna-zbran/
70 % done!
Tenkrát v Americe (88%)
	http://csfd.cz/film/5914-tenkrat-v-americe/
80 % done!
90 % done!
Uprchlík (87%)
	http://csfd.cz/film/2302-uprchlik/
```

- Uplne vsetko ;-)
```
% csfd search HBOMax --unseen-only --sorted --page=3

Logged into CSFD.cz as moj_login
10 % done!
20 % done!
30 % done!
40 % done!
50 % done!
60 % done!
70 % done!
80 % done!
90 % done!
100 % done!
Spider-Man: Paralelní světy (85%)
	http://csfd.cz/film/54763-spider-man-paralelni-svety/
Joker (85%)
	http://csfd.cz/film/628813-joker/
Whiplash (85%)
	http://csfd.cz/film/358992-whiplash/
JFK (85%)
	http://csfd.cz/film/8755-jfk/
Musíme si pomáhat (85%)
	http://csfd.cz/film/4567-musime-si-pomahat/
Na sever Severozápadní linkou (85%)
	http://csfd.cz/film/4379-na-sever-severozapadni-linkou/
```