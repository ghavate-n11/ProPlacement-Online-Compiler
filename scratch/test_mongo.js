// In-memory MongoDB Shell Execution Engine for Node.js
function createMongoSandbox() {
  const collections = {};

  function getCollection(name) {
    if (!collections[name]) {
      collections[name] = [];
    }
    return collections[name];
  }

  function matchesFilter(doc, filter) {
    if (!filter || Object.keys(filter).length === 0) return true;
    for (let key in filter) {
      const cond = filter[key];
      if (typeof cond === 'object' && cond !== null && !Array.isArray(cond)) {
        for (let op in cond) {
          const val = cond[op];
          if (op === '$gt' && !(doc[key] > val)) return false;
          if (op === '$gte' && !(doc[key] >= val)) return false;
          if (op === '$lt' && !(doc[key] < val)) return false;
          if (op === '$lte' && !(doc[key] <= val)) return false;
          if (op === '$ne' && doc[key] === val) return false;
          if (op === '$in' && (!Array.isArray(val) || !val.includes(doc[key]))) return false;
          if (op === '$nin' && Array.isArray(val) && val.includes(doc[key])) return false;
          if (op === '$regex') {
            const re = new RegExp(val, cond.$options || '');
            if (!re.test(String(doc[key]))) return false;
          }
        }
      } else {
        if (doc[key] !== cond) return false;
      }
    }
    return true;
  }

  function generateObjectId() {
    return 'ObjectId("' + Math.random().toString(16).substring(2, 10) + Math.random().toString(16).substring(2, 10) + Math.random().toString(16).substring(2, 10) + '")';
  }

  class CollectionProxy {
    constructor(name) {
      this.name = name;
    }

    insertOne(doc) {
      const data = getCollection(this.name);
      const newDoc = { _id: doc._id || generateObjectId(), ...doc };
      data.push(newDoc);
      return {
        acknowledged: true,
        insertedId: newDoc._id
      };
    }

    insertMany(docs) {
      const data = getCollection(this.name);
      const insertedIds = {};
      docs.forEach((doc, idx) => {
        const newDoc = { _id: doc._id || generateObjectId(), ...doc };
        data.push(newDoc);
        insertedIds[idx] = newDoc._id;
      });
      return {
        acknowledged: true,
        insertedCount: docs.length,
        insertedIds
      };
    }

    insert(docOrDocs) {
      if (Array.isArray(docOrDocs)) {
        return this.insertMany(docOrDocs);
      }
      return this.insertOne(docOrDocs);
    }

    find(filter = {}, projection = null) {
      const data = getCollection(this.name);
      let results = data.filter(doc => matchesFilter(doc, filter));

      const cursor = {
        _results: [...results],
        sort(sortObj) {
          cursor._results.sort((a, b) => {
            for (let k in sortObj) {
              const dir = sortObj[k];
              if (a[k] < b[k]) return -1 * dir;
              if (a[k] > b[k]) return 1 * dir;
            }
            return 0;
          });
          return cursor;
        },
        limit(n) {
          cursor._results = cursor._results.slice(0, n);
          return cursor;
        },
        skip(n) {
          cursor._results = cursor._results.slice(n);
          return cursor;
        },
        toArray() {
          return cursor._results;
        },
        count() {
          return cursor._results.length;
        },
        forEach(fn) {
          cursor._results.forEach(fn);
        },
        pretty() {
          return cursor._results;
        }
      };

      // Custom inspect for console output
      cursor[Symbol.for('nodejs.util.inspect.custom')] = function () {
        return cursor._results;
      };

      return cursor;
    }

    findOne(filter = {}) {
      const data = getCollection(this.name);
      return data.find(doc => matchesFilter(doc, filter)) || null;
    }

    updateOne(filter, update) {
      const data = getCollection(this.name);
      const target = data.find(doc => matchesFilter(doc, filter));
      if (!target) return { acknowledged: true, matchedCount: 0, modifiedCount: 0 };

      if (update.$set) {
        Object.assign(target, update.$set);
      }
      if (update.$inc) {
        for (let k in update.$inc) {
          target[k] = (target[k] || 0) + update.$inc[k];
        }
      }
      return { acknowledged: true, matchedCount: 1, modifiedCount: 1 };
    }

    updateMany(filter, update) {
      const data = getCollection(this.name);
      const targets = data.filter(doc => matchesFilter(doc, filter));
      targets.forEach(target => {
        if (update.$set) Object.assign(target, update.$set);
        if (update.$inc) {
          for (let k in update.$inc) target[k] = (target[k] || 0) + update.$inc[k];
        }
      });
      return { acknowledged: true, matchedCount: targets.length, modifiedCount: targets.length };
    }

    deleteOne(filter) {
      const data = getCollection(this.name);
      const idx = data.findIndex(doc => matchesFilter(doc, filter));
      if (idx !== -1) {
        data.splice(idx, 1);
        return { acknowledged: true, deletedCount: 1 };
      }
      return { acknowledged: true, deletedCount: 0 };
    }

    deleteMany(filter) {
      const data = getCollection(this.name);
      const before = data.length;
      collections[this.name] = data.filter(doc => !matchesFilter(doc, filter));
      return { acknowledged: true, deletedCount: before - collections[this.name].length };
    }

    countDocuments(filter = {}) {
      const data = getCollection(this.name);
      return data.filter(doc => matchesFilter(doc, filter)).length;
    }

    aggregate(pipeline = []) {
      let current = [...getCollection(this.name)];
      for (let stage of pipeline) {
        if (stage.$match) {
          current = current.filter(doc => matchesFilter(doc, stage.$match));
        }
        if (stage.$group) {
          const groupKey = stage.$group._id;
          const groups = {};
          current.forEach(doc => {
            let keyVal = groupKey;
            if (typeof groupKey === 'string' && groupKey.startsWith('$')) {
              keyVal = doc[groupKey.slice(1)];
            }
            if (!groups[keyVal]) {
              groups[keyVal] = { _id: keyVal };
              for (let f in stage.$group) {
                if (f === '_id') continue;
                groups[keyVal][f] = 0;
              }
            }
            for (let f in stage.$group) {
              if (f === '_id') continue;
              const acc = stage.$group[f];
              if (acc.$sum) {
                const addVal = typeof acc.$sum === 'string' && acc.$sum.startsWith('$') ? (doc[acc.$sum.slice(1)] || 0) : acc.$sum;
                groups[keyVal][f] += addVal;
              } else if (acc.$avg) {
                const addVal = typeof acc.$avg === 'string' && acc.$avg.startsWith('$') ? (doc[acc.$avg.slice(1)] || 0) : acc.$avg;
                groups[keyVal][f] = (groups[keyVal][f] || 0) + addVal;
              }
            }
          });
          current = Object.values(groups);
        }
        if (stage.$sort) {
          current.sort((a, b) => {
            for (let k in stage.$sort) {
              const dir = stage.$sort[k];
              if (a[k] < b[k]) return -1 * dir;
              if (a[k] > b[k]) return 1 * dir;
            }
            return 0;
          });
        }
        if (stage.$limit) {
          current = current.slice(0, stage.$limit);
        }
      }
      return {
        toArray() { return current; },
        [Symbol.for('nodejs.util.inspect.custom')]() { return current; }
      };
    }
  }

  const db = new Proxy({}, {
    get(target, prop) {
      if (typeof prop === 'string') {
        return new CollectionProxy(prop);
      }
      return target[prop];
    }
  });

  return { db, ObjectId: (id) => `ObjectId("${id || Math.random().toString(16).slice(2, 10)}")` };
}

// Test Mongo Code Runner
const { db, ObjectId } = createMongoSandbox();

console.log("=== MongoDB Shell Test ===");
db.students.insertMany([
  { name: "Rahul Verma", score: 92, branch: "CSE" },
  { name: "Raj Roy", score: 88, branch: "IT" },
  { name: "Karan Patel", score: 95, branch: "CSE" },
  { name: "Nilesh Ghavate", score: 79, branch: "ECE" }
]);

console.log("--- Find High Scorers (score >= 90) ---");
const highScorers = db.students.find({ score: { $gte: 90 } }).toArray();
console.log(JSON.stringify(highScorers, null, 2));

console.log("\n--- Aggregation by Branch ---");
const branchStats = db.students.aggregate([
  { $match: { score: { $gte: 80 } } },
  { $group: { _id: "$branch", count: { $sum: 1 }, totalScore: { $sum: "$score" } } },
  { $sort: { totalScore: -1 } }
]).toArray();
console.log(JSON.stringify(branchStats, null, 2));
