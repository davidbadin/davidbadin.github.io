/**
 * Convert an XML string into a JSON-serializable JavaScript object.
 *
 * Conventions:
 *   - Attributes are flattened onto the element object alongside child keys.
 *   - When an element has both attributes/children AND text, the text is stored under "_text".
 *   - Pure-text elements (no attributes, no children) collapse to their scalar value.
 *   - Tags listed in KNOWN_REPEATING_TAGS are always returned as arrays, even if a single occurrence.
 *   - Numeric strings (integers / decimals) are auto-parsed to numbers; everything else stays a string.
 *   - Dashes in tag and attribute names are converted to underscores so dot notation works
 *     (e.g. <poll-summary> becomes the key "poll_summary").
 *
 * Universal: pure JavaScript, zero dependencies. Works in browsers and Node.js.
 *
 * @param {string} xml - the XML source text
 * @param {{ repeatingTags?: string[] }} [options] - optional list of additional tag names that should always be arrays
 * @returns {object} - JSON-shaped object representing the XML document
 */
function xmlToJson(xml, options = {}) {
  // Convert dashed names like "poll-summary" into JS-friendly "poll_summary"
  // so the result can be accessed with dot notation.
  const sanitizeName = (n) => n.replace(/-/g, '_');

  // Tags that should always be wrapped in an array, even when only one occurrence is present.
  // Tuned for the BoardGameGeek XML API; pass options.repeatingTags to extend this list.
  // Names are stored in their sanitized (underscore) form to match the parsed output.
  const KNOWN_REPEATING_TAGS = new Set([
    'boardgame', 'name',
    'boardgamedesigner', 'boardgameartist',
    'boardgamecategory', 'boardgameversion',
    'boardgamepublisher', 'boardgamemechanic',
    'boardgamefamily', 'boardgamehonor',
    'boardgamesubdomain', 'boardgameexpansion',
    'boardgamepodcastepisode', 'boardgameintegration',
    'boardgameaccessory', 'boardgameimplementation',
    'cardset',
    'poll', 'poll_summary',
    'results', 'result', 'rank',
    ...((options.repeatingTags || []).map(sanitizeName)),
  ]);

  // Strip XML declaration and comments before parsing.
  const src = String(xml)
    .replace(/<\?xml[\s\S]*?\?>/g, '')
    .replace(/<!--[\s\S]*?-->/g, '');

  let pos = 0;

  function decodeEntities(s) {
    return s
      .replace(/&lt;/g, '<')
      .replace(/&gt;/g, '>')
      .replace(/&quot;/g, '"')
      .replace(/&apos;/g, "'")
      .replace(/&#x([0-9a-fA-F]+);/g, (_, h) => String.fromCharCode(parseInt(h, 16)))
      .replace(/&#(\d+);/g, (_, d) => String.fromCharCode(parseInt(d, 10)))
      .replace(/&amp;/g, '&'); // must run last
  }

  function coerce(v) {
    if (typeof v !== 'string') return v;
    if (/^-?\d+$/.test(v)) {
      const n = Number(v);
      if (Number.isSafeInteger(n)) return n;
    }
    if (/^-?(?:\d+\.\d*|\.\d+|\d+\.\d+)$/.test(v)) {
      const n = parseFloat(v);
      if (!Number.isNaN(n)) return n;
    }
    return v;
  }

  function skipWhitespace() {
    while (pos < src.length && /\s/.test(src[pos])) pos++;
  }

  function parseAttributes() {
    const attrs = {};
    while (pos < src.length) {
      skipWhitespace();
      const c = src[pos];
      if (c === '/' || c === '>' || c === undefined) break;

      const nameStart = pos;
      while (pos < src.length && !/[\s=/>]/.test(src[pos])) pos++;
      const name = sanitizeName(src.slice(nameStart, pos));

      skipWhitespace();
      let value = '';
      if (src[pos] === '=') {
        pos++;
        skipWhitespace();
        const quote = src[pos];
        if (quote === '"' || quote === "'") {
          pos++;
          const vStart = pos;
          while (pos < src.length && src[pos] !== quote) pos++;
          value = decodeEntities(src.slice(vStart, pos));
          pos++; // skip closing quote
        } else {
          // Unquoted attribute (rare, but tolerate it)
          const vStart = pos;
          while (pos < src.length && !/[\s/>]/.test(src[pos])) pos++;
          value = decodeEntities(src.slice(vStart, pos));
        }
      }
      attrs[name] = coerce(value);
    }
    return attrs;
  }

  function parseElement() {
    if (src[pos] !== '<') return null;
    pos++; // consume '<'

    const nameStart = pos;
    while (pos < src.length && !/[\s/>]/.test(src[pos])) pos++;
    const tagName = sanitizeName(src.slice(nameStart, pos));

    const attrs = parseAttributes();

    let selfClosing = false;
    if (src[pos] === '/') {
      selfClosing = true;
      pos++;
    }
    if (src[pos] === '>') pos++;

    let textContent = '';
    const children = [];

    if (!selfClosing) {
      while (pos < src.length) {
        if (src[pos] === '<') {
          // CDATA section
          if (src.startsWith('<![CDATA[', pos)) {
            pos += 9;
            const end = src.indexOf(']]>', pos);
            textContent += src.slice(pos, end === -1 ? src.length : end);
            pos = end === -1 ? src.length : end + 3;
            continue;
          }
          // Inline comment
          if (src.startsWith('<!--', pos)) {
            const end = src.indexOf('-->', pos);
            pos = end === -1 ? src.length : end + 3;
            continue;
          }
          // Closing tag
          if (src[pos + 1] === '/') {
            pos += 2;
            while (pos < src.length && src[pos] !== '>') pos++;
            pos++; // consume '>'
            break;
          }
          // Child element
          const child = parseElement();
          if (child) children.push(child);
        } else {
          textContent += src[pos];
          pos++;
        }
      }
    }

    return { tagName, value: buildValue(attrs, children, textContent) };
  }

  function buildValue(attrs, children, rawText) {
    const text = decodeEntities(rawText).trim();
    const hasAttrs = Object.keys(attrs).length > 0;
    const hasChildren = children.length > 0;

    // Pure scalar element: <yearpublished>1996</yearpublished>  ->  1996
    if (!hasAttrs && !hasChildren) {
      return text === '' ? '' : coerce(text);
    }

    const out = { ...attrs };

    if (hasChildren) {
      const grouped = {};
      for (const c of children) {
        if (Object.prototype.hasOwnProperty.call(grouped, c.tagName)) {
          if (!Array.isArray(grouped[c.tagName])) {
            grouped[c.tagName] = [grouped[c.tagName]];
          }
          grouped[c.tagName].push(c.value);
        } else {
          grouped[c.tagName] = c.value;
        }
      }
      // Force known-repeating tags into arrays even when only one is present.
      for (const tag of Object.keys(grouped)) {
        if (KNOWN_REPEATING_TAGS.has(tag) && !Array.isArray(grouped[tag])) {
          grouped[tag] = [grouped[tag]];
        }
      }
      Object.assign(out, grouped);
    }

    if (text !== '') {
      out._text = coerce(text);
    }

    return out;
  }

  skipWhitespace();
  const root = parseElement();
  if (!root) return {};
  return { [root.tagName]: root.value };
}

// Export for Node.js / CommonJS. In browsers, just use the global function.
if (typeof module !== 'undefined' && module.exports) {
  module.exports = xmlToJson;
}
