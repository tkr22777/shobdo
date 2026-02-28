// Merged from StringBuilder.js + ridmikmap.js + ridmikparser.js
// Single ES module — export { RidmikParser }

function StringBuilder(str) {
  this._str = str == null ? '' : str;

  this.append = function (s) {
    this._str += s;
    return this;
  };

  this.remove = function (from /*, to */) {
    this._str = this._str.slice(0, from);
    return this;
  };

  this.length = function () {
    return this._str.length;
  };

  this.toString = function () {
    return this._str;
  };
}

function BanglaUnicode() {
  var m = {};
  var k = {};
  var jkt = {};
  var djkt = {};
  var djktt = {};

  // character map
  m['o'] = '\u0985';
  m['O'] = '\u0993';
  m['a'] = '\u0986';
  m['A'] = '\u0986';
  m['S'] = '\u09B6';
  m['sh'] = '\u09B6';
  m['s'] = '\u09B8';
  m['Sh'] = '\u09B7';
  m['h'] = '\u09B9';
  m['H'] = '\u09B9';
  m['r'] = '\u09B0';
  m['R'] = '\u09DC';
  m['Rh'] = '\u09DD';
  m['k'] = '\u0995';
  m['K'] = '\u0995';
  m['q'] = '\u0995';
  m['qq'] = '\u0981';
  m['kh'] = '\u0996';
  m['g'] = '\u0997';
  m['G'] = '\u0997';
  m['gh'] = '\u0998';
  m['Ng'] = '\u0999';
  m['c'] = '\u099A';
  m['C'] = '\u099A';
  m['ch'] = '\u099B';
  m['j'] = '\u099C';
  m['jh'] = '\u099D';
  m['J'] = '\u099C';
  m['NG'] = '\u099E';
  m['T'] = '\u099F';
  m['Th'] = '\u09A0';
  m['TH'] = '\u09CE';
  m['f'] = '\u09AB';
  m['F'] = '\u09AB';
  m['ph'] = '\u09AB';
  m['i'] = '\u0987';
  m['I'] = '\u0988';
  m['e'] = '\u098F';
  m['E'] = '\u098F';
  m['u'] = '\u0989';
  m['U'] = '\u098A';
  m['b'] = '\u09AC';
  m['B'] = '\u09AC';
  m['w'] = '\u09AC';
  m['bh'] = '\u09AD';
  m['V'] = '\u09AD';
  m['v'] = '\u09AD';
  m['t'] = '\u09A4';
  m['th'] = '\u09A5';
  m['d'] = '\u09A6';
  m['dh'] = '\u09A7';
  m['D'] = '\u09A1';
  m['Dh'] = '\u09A2';
  m['n'] = '\u09A8';
  m['N'] = '\u09A3';
  m['z'] = '\u09AF';
  m['Z'] = '\u09AF';
  m['y'] = '\u09DF';
  m['l'] = '\u09B2';
  m['L'] = '\u09B2';
  m['m'] = '\u09AE';
  m['M'] = '\u09AE';
  m['P'] = '\u09AA';
  m['p'] = '\u09AA';
  m['ng'] = '\u0982';
  m['cb'] = '\u0981';
  m['x'] = '\u0995\u09CD\u09B8';
  m['OU'] = '\u0994';
  m['OI'] = '\u0990';
  m['hs'] = '\u09CD';
  m['nj'] = '\u099E\u09CD\u099C';
  m['nc'] = '\u099E\u09CD\u099A';

  // kar map
  k['o'] = '';
  k['a'] = '\u09BE';
  k['A'] = '\u09BE';
  k['e'] = '\u09C7';
  k['E'] = '\u09C7';
  k['O'] = '\u09CB';
  k['OI'] = '\u09C8';
  k['OU'] = '\u09CC';
  k['i'] = '\u09BF';
  k['I'] = '\u09C0';
  k['u'] = '\u09C1';
  k['U'] = '\u09C2';
  k['oo'] = '\u09C1';

  // jkt
  jkt['k'] = 'kTtnNslw';
  jkt['g'] = 'gnNmlw';
  jkt['ch'] = 'w';
  jkt['Ng'] = 'gkm';
  jkt['NG'] = 'cj';
  jkt['g'] = 'gnNmlw';
  jkt['G'] = 'gnNmlw';
  jkt['th'] = 'w';
  jkt['gh'] = 'Nn';
  jkt['c'] = 'c';
  jkt['j'] = 'jw';
  jkt['T'] = 'T';
  jkt['D'] = 'D';
  jkt['R'] = 'g';
  jkt['N'] = 'DNmw';
  jkt['t'] = 'tnmwN';
  jkt['d'] = 'wdm';
  jkt['dh'] = 'wn';
  jkt['n'] = 'ndwmtsDT';
  jkt['p'] = 'plTtns';
  jkt['f'] = 'l';
  jkt['ph'] = 'l';
  jkt['b'] = 'jdbwl';
  jkt['v'] = 'l';
  jkt['bh'] = 'l';
  jkt['m'] = 'npfwvmlb';
  jkt['l'] = 'lwmpkgTDf';
  jkt['Sh'] = 'kTNpmf';
  jkt['S'] = 'clwnm';
  jkt['sh'] = 'clwnm';
  jkt['s'] = 'kTtnpfmlw';
  jkt['h'] = 'Nnmlw';
  jkt['cb'] = '';
  jkt['jh'] = '';
  jkt['TH'] = '';
  jkt['qq'] = '';
  jkt['ng'] = '';
  jkt['kh'] = '';
  jkt['gg'] = '';
  jkt['dh'] = '';
  jkt['Th'] = '';

  // djkt
  djkt['kh'] = 'Ngs';
  djkt['ch'] = 'c';
  djkt['Dh'] = 'N';
  djkt['ph'] = 'mls';
  djkt['dh'] = 'gdnbl';
  djkt['bh'] = 'dm';
  djkt['Sh'] = 'k';
  djkt['th'] = 'tns';
  djkt['Th'] = 'Nn';
  djkt['jh'] = 'j';
  djkt['NG'] = 'cj';

  // djktt
  djktt['ch'] = 'NG';
  djktt['gh'] = 'Ng';
  djktt['Th'] = 'Sh';
  djktt['jh'] = 'NG';
  djktt['sh'] = 'ch';

  this.getDual = function (x, carry) { return m[carry + x]; };
  this.get = function (x) { return m[x]; };
  this.getKar = function (x) { return k[x]; };
  this.getDualKar = function (x, carry) { return k[carry + x]; };
  this.getJkt = function (carry) { return jkt[carry]; };
  this.getDualJkt = function (secondCarry, carry) { return jkt[secondCarry + carry]; };
  this.getDjkt = function (secondCarry, carry) { return djkt[secondCarry + carry]; };
  this.getDjktt = function (secondCarry, carry) { return djktt[secondCarry + carry]; };
}

function RidmikParser() {
  var unicode = new BanglaUnicode();

  this.toBangla = function (engWord) {
    var st = new StringBuilder('');
    var carry = 0;
    var secondCarry = 0;
    var thirdCarry = 0;
    var tempNoCarry = false;
    var jukta = false;
    var prevJukta = false;

    for (var p in engWord) {
      var now = engWord[p];

      if (!((now >= 'a' && now <= 'z') || (now >= 'A' && now <= 'Z') || (now >= '0' && now <= '9'))) {
        st.append(now);
        carry = 0;
        continue;
      }

      if (now === 'A' || now === 'B' || now === 'C' || now === 'E' || now === 'F' || now === 'P' || now === 'X')
        now = now.toLowerCase();
      if (now === 'K' || now === 'L' || now === 'M' || now === 'V' || now === 'Y' || now === 'W' || now === 'Q')
        now = now.toLowerCase();

      if (now === 'H' && carry !== 'T')
        now = 'h';

      if ((carry === 0 || isVowel(carry)) && now === 'w')
        now = 'O';

      if (isVowel(now)) {
        if (carry === 'r' && secondCarry === 'r' && now === 'i') {
          if (thirdCarry === 0) {
            st.remove(st.length() - 2);
            st.append('\u098B');
          } else {
            st.remove(st.length() - 3);
            st.append('\u09C3');
          }
          carry = 'i';
          continue;
        }

        var dual;
        if (secondCarry !== 0)
          dual = unicode.getDualKar(now, carry);
        else dual = unicode.getDual(now, carry);

        if (dual !== undefined) {
          if (carry !== 'o')
            st.remove(st.length() - 1);
          if (isVowel(secondCarry)) {
            st.append(unicode.get(carry)).append(unicode.get(now));
          } else
            st.append(dual);
        } else if (now === 'o' && carry !== 0) {
          if (isVowel(carry))
            st.append(unicode.get('O'));
          else {
            thirdCarry = secondCarry;
            secondCarry = carry;
            carry = now;
            continue;
          }
        } else if (isVowel(carry) || carry === 0) {
          if (now === 'a' && carry !== 0)
            st.append(unicode.get('y')).append(unicode.getKar('a'));
          else
            st.append(unicode.get(now));
        } else {
          st.append(unicode.getKar(now));
        }
      }

      if (now === 'y' || now === 'Z' || now === 'r')
        jukta = false;

      tempNoCarry = jukta && unicode.getDual(now, carry) === undefined;

      if (isConsonant(now) && isConsonant(carry) && !tempNoCarry) {
        if (now === 'y' || now === 'Z') {
          if (now === 'y' && carry === 'q' && secondCarry === 'q');
          else now = 'z';
        }

        if (carry === 'g' && now === 'g' && secondCarry !== 'N' && secondCarry !== 'n') {
          st.remove(st.length() - 1);
          st.append('\u099C\u09CD\u099E');
          prevJukta = jukta;
          jukta = true;
          secondCarry = 'g';
          continue;
        }

        if (secondCarry === 'k' && carry === 'k' && now === 'h')
          carry = 'S';

        var dual = unicode.getDual(now, carry);

        if (dual !== undefined) {
          if (thirdCarry === 'g' && secondCarry === 'k' && carry === 'S' && now === 'h')
            prevJukta = jukta = false;

          var firstOrAfterVowelOrJukta = isVowel(secondCarry) || secondCarry === 0 || prevJukta;

          if (dualSitsUnder(thirdCarry, secondCarry, carry, now) && !firstOrAfterVowelOrJukta) {
            st.remove(st.length() - 1);
            if (secondCarry === 'r' && thirdCarry === 'r')
              st.remove(st.length() - 1);
            if (jukta);
            else if (secondCarry !== 0 && !isVowel(secondCarry))
              st.append('\u09CD');

            st.append(dual);
            prevJukta = jukta;
            jukta = true;
          } else {
            if (jukta)
              st.remove(st.length() - 2);
            else st.remove(st.length() - 1);

            if (secondCarry === 'g' && carry === 'g' && now === 'h') {
              st.remove(st.length() - 1);
              st.append(unicode.get('g'));
            }

            st.append(dual);
            prevJukta = jukta;
            jukta = false;
          }
        } else {
          prevJukta = jukta;
          jukta = false;

          if (secondCarry !== 'r' && carry === 'r' && now === 'z') {
            st.append('\u200D\u09CD');
          } else if (carry === 'r' && secondCarry !== 'r');
          else if (carry === 'r' && secondCarry === 'r' && isConsonant(thirdCarry));
          else if (carry === 'r' && secondCarry === 'r' && (isVowel(thirdCarry) || thirdCarry === 0)) {
            st.remove(st.length() - 1);
            st.append('\u09CD');
          } else if (notJukta(thirdCarry, secondCarry, carry, now));
          else {
            st.append('\u09CD');
            jukta = true;
          }

          st.append(unicode.get(now));
        }
      } else if (isConsonant(now)) {
        if (isVowel(carry) && now === 'Z')
          st.append('\u09CD');

        if (carry === 0 && now === 'x')
          st.append(unicode.get('e'));

        prevJukta = jukta;
        jukta = false;

        if (now === 'w' && isConsonant(carry) && isConsonant(secondCarry)) {
          st.append('\u09CD');
          prevJukta = jukta;
          jukta = true;
        }
        if (thirdCarry === 'k' && secondCarry === 'S' && carry === 'h' && (now === 'N' || now === 'm')) {
          st.append('\u09CD');
          prevJukta = false;
          jukta = true;
        }
        st.append(unicode.get(now));
      }

      thirdCarry = secondCarry;
      secondCarry = carry;
      carry = now;
    }

    return st.toString();
  };

  function isVowel(now) {
    return 'AEIOUaeiou'.indexOf(now) !== -1;
  }

  function isConsonant(now) {
    return !isVowel(now) && isNaN(now);
  }

  function isCharInString(now, foo) {
    return foo.indexOf(now) !== -1;
  }

  function dualSitsUnder(thirdCarry, secondCarry, carry, now) {
    if (secondCarry === 'r' && thirdCarry === 'r') return true;
    if (secondCarry === 'r') return false;

    var d = unicode.getDjkt(carry, now);
    if (d !== undefined && isCharInString(secondCarry, d)) return true;

    var dd = unicode.getDjktt(carry, now);
    if (dd !== undefined) return isCharInString(thirdCarry + secondCarry, dd);

    return false;
  }

  function notJukta(thirdCarry, secondCarry, carry, now) {
    if (now === 'r' || now === 'z' || now === 'w') return false;

    var foo = unicode.getDualJkt(secondCarry, carry);
    if (foo !== undefined) return !isCharInString(now, foo);

    foo = unicode.getJkt(carry);
    if (foo !== undefined) return !isCharInString(now, foo);

    return true;
  }
}

export { RidmikParser };
