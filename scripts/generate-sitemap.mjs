#!/usr/bin/env node
/**
 * Generates client/react/public/sitemap.xml from the MongoDB BSON dump.
 *
 * Usage:
 *   node scripts/generate-sitemap.mjs
 *   make generate-sitemap
 *
 * No external dependencies — pure Node.js.
 */

import { readFileSync, writeFileSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));

const BSON_PATH    = resolve(__dirname, '../data/mongodump/Dictionary/Words.bson');
const SITEMAP_PATH = resolve(__dirname, '../client/react/public/sitemap.xml');
const BASE_URL     = 'https://www.shobdo.info';
const TODAY        = new Date().toISOString().split('T')[0];

// ---------------------------------------------------------------------------
// Minimal BSON reader — no external dependencies.
// Extracts only the top-level "spelling" string from each concatenated document.
// BSON spec: https://bsonspec.org/spec.html
// ---------------------------------------------------------------------------

/** Returns the new offset after skipping over a BSON value of the given type. */
function skipValue(buf, pos, type) {
  switch (type) {
    case 0x01: return pos + 8;                               // Double
    case 0x02: return pos + 4 + buf.readInt32LE(pos);        // String (4-byte len + data + null)
    case 0x03: case 0x04: return pos + buf.readInt32LE(pos); // Document / Array
    case 0x05: return pos + 4 + 1 + buf.readInt32LE(pos);    // Binary
    case 0x06: return pos;                                    // Undefined (deprecated)
    case 0x07: return pos + 12;                              // ObjectId
    case 0x08: return pos + 1;                               // Boolean
    case 0x09: return pos + 8;                               // UTC datetime
    case 0x0a: return pos;                                    // Null
    case 0x0b: {                                              // Regex (two null-terminated strings)
      while (buf[pos] !== 0) pos++; pos++;
      while (buf[pos] !== 0) pos++; pos++;
      return pos;
    }
    case 0x0c: return pos + 4 + buf.readInt32LE(pos) + 12;  // DBPointer (deprecated)
    case 0x0d: return pos + 4 + buf.readInt32LE(pos);        // JS code
    case 0x0e: return pos + 4 + buf.readInt32LE(pos);        // Symbol (deprecated)
    case 0x0f: return pos + buf.readInt32LE(pos);            // JS code w/ scope
    case 0x10: return pos + 4;                               // Int32
    case 0x11: return pos + 8;                               // Timestamp
    case 0x12: return pos + 8;                               // Int64
    case 0x13: return pos + 16;                              // Decimal128
    case 0x7f: case 0xff: return pos;                        // MaxKey / MinKey
    default:
      throw new Error(`Unknown BSON type 0x${type.toString(16)} at buffer offset ${pos - 1}`);
  }
}

function extractSpellings(buf) {
  const spellings = [];
  let offset = 0;

  while (offset + 4 <= buf.length) {
    const docSize = buf.readInt32LE(offset);
    if (docSize < 5 || offset + docSize > buf.length) break;

    const docEnd = offset + docSize - 1; // exclude terminal 0x00
    let pos = offset + 4;               // skip 4-byte size field
    let found = false;

    while (!found && pos < docEnd) {
      const type = buf[pos++];
      if (type === 0x00) break; // document terminator

      // Read null-terminated key
      const keyStart = pos;
      while (pos < docEnd && buf[pos] !== 0) pos++;
      const key = buf.slice(keyStart, pos).toString('utf8');
      pos++; // skip null terminator

      if (type === 0x02 && key === 'spelling') {
        const strLen = buf.readInt32LE(pos);
        const value  = buf.slice(pos + 4, pos + 4 + strLen - 1).toString('utf8');
        spellings.push(value);
        found = true;
      } else {
        pos = skipValue(buf, pos, type);
      }
    }

    offset += docSize;
  }

  return spellings;
}

// ---------------------------------------------------------------------------
// Sitemap XML builder
// ---------------------------------------------------------------------------

function xmlEscape(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function buildSitemap(spellings) {
  const homeEntry = [
    '  <url>',
    `    <loc>${BASE_URL}/</loc>`,
    `    <lastmod>${TODAY}</lastmod>`,
    '    <changefreq>weekly</changefreq>',
    '    <priority>1.0</priority>',
    '  </url>',
  ].join('\n');

  const wordEntries = spellings.map(s => {
    const loc = xmlEscape(`${BASE_URL}/bn/word/${encodeURIComponent(s)}`);
    return [
      '  <url>',
      `    <loc>${loc}</loc>`,
      `    <lastmod>${TODAY}</lastmod>`,
      '    <changefreq>monthly</changefreq>',
      '    <priority>0.8</priority>',
      '  </url>',
    ].join('\n');
  });

  const entries = [homeEntry, ...wordEntries].join('\n');
  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
    entries,
    '</urlset>',
    '',
  ].join('\n');
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

console.log(`Reading ${BSON_PATH} …`);
const buf = readFileSync(BSON_PATH);
const spellings = extractSpellings(buf);
console.log(`Found ${spellings.length} words.`);

const xml = buildSitemap(spellings);
writeFileSync(SITEMAP_PATH, xml, 'utf8');
console.log(`Sitemap written → ${SITEMAP_PATH}  (${spellings.length + 1} URLs)`);
