<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>AI-Driven Workforce Management — Demo</title>

  <!-- Tailwind CDN (for quick prototyping) -->
  <script src="https://cdn.tailwindcss.com"></script>

  <!-- Chart.js CDN -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

  <style>
    /* small custom adjustments */
    .card { @apply bg-white rounded-lg shadow p-4; }
    .mini { font-size: 0.85rem; }
  </style>
</head>
<body class="bg-slate-100 text-slate-800">

  <div class="max-w-7xl mx-auto p-6">
    <header class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">AI-Driven Workforce Management — Demo</h1>
        <p class="text-sm text-slate-600">Skill-based task assignment · Workforce intelligence · IoT-triggered reassign</p>
      </div>
      <div class="space-x-2">
        <button id="simulate-iot" class="bg-orange-500 text-white px-3 py-1 rounded shadow-sm text-sm">Simulate Machine Down</button>
        <button id="reset" class="bg-slate-200 px-3 py-1 rounded text-sm">Reset</button>
      </div>
    </header>

    <main class="grid grid-cols-12 gap-6">
      <!-- Left: Manager & Task Controls -->
      <section class="col-span-4">
        <div class="card mb-4">
          <h2 class="font-semibold text-lg">Create Task</h2>
          <form id="task-form" class="space-y-3 mt-3">
            <input id="task-title" class="w-full border rounded px-3 py-2" placeholder="Task title (e.g., CNC calibration)" required>
            <input id="task-duration" type="number" min="1" class="w-full border rounded px-3 py-2" placeholder="Estimated hours" required>
            <div>
              <label class="mini text-slate-700">Required skills (comma separated)</label>
              <input id="task-skills" class="w-full border rounded px-3 py-2" placeholder="welding,cnc,calibration" required>
            </div>
            <div class="flex gap-2">
              <button type="submit" class="bg-blue-600 text-white px-3 py-2 rounded">Create & Assign</button>
              <button id="create-unassigned" type="button" class="bg-slate-200 px-3 py-2 rounded">Create Unassigned</button>
            </div>
          </form>
        </div>

        <div class="card mb-4">
          <h2 class="font-semibold text-lg">Workers</h2>
          <ul id="workers-list" class="mt-3 space-y-2"></ul>
          <div class="mt-3 border-t pt-3">
            <h3 class="mini font-medium">Add worker</h3>
            <div class="flex gap-2 mt-2">
              <input id="new-name" class="w-1/2 border rounded px-2 py-1" placeholder="Name">
              <input id="new-skills" class="w-1/2 border rounded px-2 py-1" placeholder="skills (comma)">
            </div>
            <div class="flex gap-2 mt-2">
              <input id="new-rating" type="number" min="1" max="5" class="w-1/3 border rounded px-2 py-1" placeholder="rating">
              <button id="add-worker" class="bg-green-600 text-white px-3 py-1 rounded">Add</button>
            </div>
            <p class="mini text-slate-500 mt-2">Points: gamified score for motivation.</p>
          </div>
        </div>

        <div class="card">
          <h2 class="font-semibold text-lg">Unassigned Tasks</h2>
          <ul id="unassigned-list" class="mt-3 space-y-2"></ul>
        </div>
      </section>

      <!-- Middle: Dashboard -->
      <section class="col-span-5">
        <div class="card mb-4">
          <h2 class="font-semibold text-lg">Manager Dashboard</h2>
          <div class="grid grid-cols-3 gap-4 mt-3">
            <div class="bg-slate-50 p-3 rounded">
              <div class="text-sm text-slate-500">Active Tasks</div>
              <div id="stat-active" class="text-xl font-bold">0</div>
            </div>
            <div class="bg-slate-50 p-3 rounded">
              <div class="text-sm text-slate-500">Avg Match Score</div>
              <div id="stat-avgscore" class="text-xl font-bold">0</div>
            </div>
            <div class="bg-slate-50 p-3 rounded">
              <div class="text-sm text-slate-500">Idle Workers</div>
              <div id="stat-idle" class="text-xl font-bold">0</div>
            </div>
          </div>

          <div class="mt-4 grid grid-cols-2 gap-4">
            <div>
              <canvas id="skillUtilChart" height="220"></canvas>
            </div>
            <div>
              <canvas id="productivityChart" height="220"></canvas>
            </div>
          </div>
        </div>

        <div class="card">
          <h2 class="font-semibold text-lg">Assignments Timeline (live)</h2>
          <ul id="assignment-log" class="mt-3 text-sm space-y-2 max-h-56 overflow-auto"></ul>
        </div>
      </section>

      <!-- Right: Worker View & Details -->
      <section class="col-span-3">
        <div class="card mb-4">
          <h2 class="font-semibold text-lg">Worker Portal (simulate)</h2>
          <div class="space-y-2 mt-3">
            <label class="mini">Select worker</label>
            <select id="select-worker" class="w-full border rounded px-2 py-1"></select>

            <div id="worker-details" class="mt-3 p-2 border rounded bg-slate-50">
              <div><strong id="w-name">—</strong></div>
              <div class="mini text-slate-600" id="w-skills">Skills: —</div>
              <div class="mini text-slate-600">Points: <span id="w-points">0</span></div>
            </div>

            <div class="mt-2">
              <button id="accept-task" class="bg-indigo-600 text-white px-3 py-1 rounded w-full">Accept Assigned Task</button>
              <button id="complete-task" class="bg-emerald-600 text-white px-3 py-1 rounded w-full mt-2">Mark Complete</button>
            </div>
          </div>
        </div>

        <div class="card">
          <h2 class="font-semibold text-lg">Active Tasks</h2>
          <ul id="active-tasks" class="mt-3 space-y-2"></ul>
        </div>
      </section>
    </main>
  </div>

  <!-- Demo script: data, matching logic, UI binding -->
  <script>
    /***** Demo data model *****/
    const workers = [
      { id: 'w1', name: 'Ravi', skills: ['cnc','calibration'], rating:4.2, points: 120, available: true, currentTask: null },
      { id: 'w2', name: 'Anita', skills: ['welding','safety'], rating:4.6, points: 210, available: true, currentTask: null },
      { id: 'w3', name: 'Sunil', skills: ['assembly','inspection'], rating:4.0, points: 65, available: true, currentTask: null },
    ];

    const tasks = []; // assigned tasks
    const unassigned = []; // tasks waiting assignment
    const assignmentLog = [];

    /***** Utility helpers *****/
    function uuid(prefix='id') { return prefix + Math.random().toString(36).slice(2,9); }
    function now() { return new Date().toLocaleTimeString(); }

    /***** Simple skill matching algorithm *****/
    // Score = skillOverlap / requiredSkills * (0.5 + rating/10) * availabilityBonus
    function matchScore(worker, task) {
      const workerSet = new Set(worker.skills.map(s=>s.toLowerCase().trim()));
      const required = task.skills.map(s=>s.toLowerCase().trim());
      const overlap = required.filter(s=>workerSet.has(s)).length;
      const base = overlap / Math.max(1, required.length);
      const ratingFactor = 0.5 + (worker.rating / 10); // between 0.6..1.0 approx
      const availability = worker.available ? 1.0 : 0.1;
      return Math.round(base * ratingFactor * availability * 100) / 100;
    }

    function autoAssignTask(task) {
      // compute scores
      let best = null;
      let bestScore = -1;
      workers.forEach(w=>{
        const sc = matchScore(w, task);
        if (sc > bestScore) { best = w; bestScore = sc; }
      });

      if (bestScore > 0.0 && best.available) {
        // assign
        task.assignedTo = best.id;
        task.matchScore = bestScore;
        task.status = 'assigned';
        task.assignedAt = now();
        tasks.push(task);
        best.currentTask = task.id;
        best.available = false;
        assignmentLog.unshift(\`[\${now()}] Assigned "\${task.title}" → \${best.name} (score \${bestScore})\`);
        return true;
      } else {
        // push to unassigned
        unassigned.push(task);
        assignmentLog.unshift(\`[\${now()}] Could not assign "\${task.title}" — moved to unassigned\`);
        return false;
      }
    }

    /***** UI render functions *****/
    function renderWorkers() {
      const el = document.getElementById('workers-list');
      el.innerHTML = '';
      workers.forEach(w=>{
        const li = document.createElement('li');
        li.className = 'flex items-center justify-between p-2 border rounded';
        li.innerHTML = \`
          <div>
            <div class="font-medium">\${w.name}</div>
            <div class="mini text-slate-600">Skills: \${w.skills.join(', ')}</div>
            <div class="mini text-slate-500">Rating: \${w.rating} • Points: \${w.points}</div>
          </div>
          <div class="flex flex-col items-end">
            <div class="mini mb-1">\${w.available ? '<span class="text-green-600 font-semibold">Available</span>' : '<span class="text-orange-600 font-semibold">Busy</span>'}</div>
            <button data-id="\${w.id}" class="bg-red-100 px-2 py-1 rounded text-sm remove-worker">Remove</button>
          </div>\`;
        el.appendChild(li);
      });

      // bind remove
      document.querySelectorAll('.remove-worker').forEach(btn=>{
        btn.onclick = e => {
          const id = btn.dataset.id;
          const idx = workers.findIndex(x=>x.id===id);
          if(idx>=0) workers.splice(idx,1);
          renderAll();
        };
      });

      // worker selector
      const sel = document.getElementById('select-worker');
      sel.innerHTML = '';
      workers.forEach(w=>{
        const opt = document.createElement('option');
        opt.value = w.id;
        opt.textContent = w.name;
        sel.appendChild(opt);
      });
      sel.onchange();
    }

    function renderUnassigned() {
      const el = document.getElementById('unassigned-list');
      el.innerHTML = '';
      unassigned.forEach(t=>{
        const li = document.createElement('li');
        li.className = 'flex items-center justify-between p-2 border rounded';
        li.innerHTML = \`<div><div class="font-medium">\${t.title}</div><div class="mini text-slate-500">skills: \${t.skills.join(', ')}</div></div>
          <div class="flex gap-2">
            <button data-id="\${t.id}" class="bg-blue-600 text-white px-2 py-1 rounded reassign">Try Assign</button>
            <button data-id="\${t.id}" class="bg-slate-200 px-2 py-1 rounded delete-task">Delete</button>
          </div>\`;
        el.appendChild(li);
      });

      document.querySelectorAll('.reassign').forEach(b=>{
        b.onclick = () => {
          const id = b.dataset.id;
          const idx = unassigned.findIndex(t=>t.id===id);
          if(idx>=0) {
            const t = unassigned.splice(idx,1)[0];
            autoAssignTask(t);
            renderAll();
          }
        };
      });
      document.querySelectorAll('.delete-task').forEach(b=>{
        b.onclick = () => {
          const id = b.dataset.id;
          const idx = unassigned.findIndex(t=>t.id===id);
          if(idx>=0){ unassigned.splice(idx,1); renderAll(); }
        }
      });
    }

    function renderActiveTasks() {
      const el = document.getElementById('active-tasks');
      el.innerHTML = '';
      tasks.forEach(t=>{
        const worker = workers.find(w=>w.id===t.assignedTo);
        const li = document.createElement('li');
        li.className = 'p-2 border rounded flex items-start justify-between';
        li.innerHTML = \`<div>
          <div class="font-medium">\${t.title} <span class="mini text-slate-500">(\${t.status})</span></div>
          <div class="mini text-slate-600">Req: \${t.skills.join(', ')} • ETA: \${t.duration}h</div>
          <div class="mini text-slate-500">Assigned: \${worker ? worker.name : '—'} • score: \${t.matchScore ?? '—'}</div>
        </div>
        <div class="flex flex-col gap-2">
          <button data-id="\${t.id}" class="bg-yellow-100 px-2 py-1 rounded btn-reassign text-sm">Reassign</button>
          <button data-id="\${t.id}" class="bg-slate-200 px-2 py-1 rounded btn-cancel text-sm">Cancel</button>
        </div>\`;
        el.appendChild(li);
      });

      document.querySelectorAll('.btn-reassign').forEach(b=>{
        b.onclick = () => {
          const id = b.dataset.id;
          const i = tasks.findIndex(t=>t.id===id);
          if(i>=0){
            const t = tasks.splice(i,1)[0];
            // free previous worker
            const prev = workers.find(w=>w.id===t.assignedTo);
            if(prev){ prev.available=true; prev.currentTask=null; }
            unassigned.push(t);
            assignmentLog.unshift(\`[\${now()}] Reassigned "\${t.title}" (moved to unassigned)\`);
            renderAll();
          }
        }
      });
      document.querySelectorAll('.btn-cancel').forEach(b=>{
        b.onclick = () => {
          const id = b.dataset.id;
          const i = tasks.findIndex(t=>t.id===id);
          if(i>=0){
            const t = tasks.splice(i,1)[0];
            const prev = workers.find(w=>w.id===t.assignedTo);
            if(prev){ prev.available=true; prev.currentTask=null; }
            assignmentLog.unshift(\`[\${now()}] Cancelled "\${t.title}"\`);
            renderAll();
          }
        }
      });
    }

    function renderAssignmentLog() {
      const el = document.getElementById('assignment-log');
      el.innerHTML = '';
      assignmentLog.slice(0,80).forEach(l=>{
        const li = document.createElement('li');
        li.className = 'text-xs text-slate-700';
        li.textContent = l;
        el.appendChild(li);
      });
    }

    function renderStatsAndCharts() {
      // stats
      document.getElementById('stat-active').textContent = tasks.length;
      const avgScore = tasks.length ? (Math.round(tasks.reduce((s,t)=>s+(t.matchScore||0),0)/tasks.length*100)/100) : 0;
      document.getElementById('stat-avgscore').textContent = avgScore;
      const idle = workers.filter(w=>w.available).length;
      document.getElementById('stat-idle').textContent = idle;

      // skill utilization: count how often each skill is in use by assigned tasks
      const skillCount = {};
      tasks.forEach(t=> t.skills.forEach(s=>{
        skillCount[s] = (skillCount[s]||0) + 1;
      }));
      // also include worker skills for denominator
      workers.forEach(w=> w.skills.forEach(s=> skillCount[s] = skillCount[s] || 0));

      const labels = Object.keys(skillCount);
      const data = labels.map(k => skillCount[k]);

      // create/update charts (destroy & recreate for simplicity)
      if(window.skillChart) window.skillChart.destroy();
      const ctx = document.getElementById('skillUtilChart').getContext('2d');
      window.skillChart = new Chart(ctx, {
        type: 'bar',
        data: { labels, datasets: [{ label: 'Skill utilization (active tasks)', data }] },
        options: { plugins: { legend: { display: false } } }
      });

      // productivity chart (points per worker)
      const pLabels = workers.map(w=>w.name);
      const pData = workers.map(w=>w.points);
      if(window.prodChart) window.prodChart.destroy();
      const ctx2 = document.getElementById('productivityChart').getContext('2d');
      window.prodChart = new Chart(ctx2, {
        type: 'doughnut',
        data: { labels: pLabels, datasets: [{ label: 'Points', data: pData }] },
      });
    }

    function renderWorkerPortal() {
      const sel = document.getElementById('select-worker');
      const id = sel.value;
      const w = workers.find(x=>x.id===id);
      if(!w) return;
      document.getElementById('w-name').textContent = w.name;
      document.getElementById('w-skills').textContent = 'Skills: ' + w.skills.join(', ');
      document.getElementById('w-points').textContent = w.points;
    }

    /***** Binding events *****/
    document.getElementById('task-form').addEventListener('submit', e=>{
      e.preventDefault();
      const title = document.getElementById('task-title').value.trim();
      const duration = Number(document.getElementById('task-duration').value) || 1;
      const skills = document.getElementById('task-skills').value.split(',').map(s=>s.trim()).filter(Boolean);
      const task = { id: uuid('task'), title, skills, duration, createdAt: now(), status: 'new' };
      autoAssignTask(task);
      // clear form
      e.target.reset();
      renderAll();
    });

    document.getElementById('create-unassigned').onclick = ()=>{
      const title = document.getElementById('task-title').value.trim() || 'Manual task';
      const duration = Number(document.getElementById('task-duration').value) || 1;
      const skills = document.getElementById('task-skills').value.split(',').map(s=>s.trim()).filter(Boolean);
      const task = { id: uuid('task'), title, skills, duration, createdAt: now(), status: 'unassigned' };
      unassigned.push(task);
      assignmentLog.unshift(\`[\${now()}] Created unassigned task "\${title}"\`);
      document.getElementById('task-form').reset();
      renderAll();
    };

    document.getElementById('add-worker').onclick = ()=>{
      const name = document.getElementById('new-name').value.trim() || 'New';
      const skills = document.getElementById('new-skills').value.split(',').map(s=>s.trim()).filter(Boolean);
      const rating = Math.max(1, Math.min(5, Number(document.getElementById('new-rating').value) || 4));
      const w = { id: uuid('w'), name, skills, rating, points: 0, available:true, currentTask:null };
      workers.push(w);
      document.getElementById('new-name').value = '';
      document.getElementById('new-skills').value = '';
      document.getElementById('new-rating').value = '';
      renderAll();
    };

    document.getElementById('select-worker').onchange = renderWorkerPortal;

    document.getElementById('accept-task').onclick = ()=>{
      const sel = document.getElementById('select-worker');
      const w = workers.find(x=>x.id===sel.value);
      if(!w) { alert('Select a worker'); return; }
      if(!w.currentTask){
        alert('No assigned task to accept (for demo, assignment happens automatically).');
        return;
      }
      assignmentLog.unshift(\`[\${now()}] \${w.name} accepted task \${w.currentTask}\`);
      renderAll();
    };

    document.getElementById('complete-task').onclick = ()=>{
      const sel = document.getElementById('select-worker');
      const w = workers.find(x=>x.id===sel.value);
      if(!w) { alert('Select a worker'); return; }
      if(!w.currentTask){
        alert('No active task');
        return;
      }
      const tid = w.currentTask;
      const idx = tasks.findIndex(t=>t.id===tid);
      if(idx>=0){
        const t = tasks.splice(idx,1)[0];
        assignmentLog.unshift(\`[\${now()}] \${w.name} completed "\${t.title}" — +\${Math.round((t.matchScore||0)*10)} pts\`);
        // award points based on matchScore & duration
        w.points += Math.round((t.matchScore||0)*10) + Math.max(1,t.duration)*2;
        w.currentTask = null;
        w.available = true;
      }
      renderAll();
    };

    document.getElementById('simulate-iot').onclick = ()=>{
      // Simulate a machine downtime requiring immediate reassignment:
      assignmentLog.unshift(\`[\${now()}] IoT: Machine X reported downtime — urgent task created\`);
      const urgent = { id: uuid('task'), title: 'Urgent: Machine X fix', skills: ['maintenance','safety'], duration: 2, createdAt: now(), status: 'new' };
      // Try to move to best available — allow reassigning busy worker if match score is much higher
      let candidate = null, candidateScore = -1;
      workers.forEach(w=>{
        const sc = matchScore(w, urgent);
        if (sc > candidateScore) { candidate = w; candidateScore = sc; }
      });

      if(candidate){
        // if best is busy, try to preempt if the score advantage is > 0.5
        if(!candidate.available){
          // find currently assigned task match score
          const currentTask = tasks.find(t=>t.id===candidate.currentTask);
          const currScore = currentTask ? matchScore(candidate, currentTask) : 0;
          if(candidateScore - currScore > 0.5){
            // preempt: move current task to unassigned
            if(currentTask){
              tasks.splice(tasks.findIndex(t=>t.id===currentTask.id),1);
              unassigned.push(currentTask);
              assignmentLog.unshift(\`[\${now()}] Preempted \${candidate.name} from "\${currentTask.title}"\`);
            }
            autoAssignTask(urgent);
          } else {
            // cannot preempt; put urgent into unassigned and notify
            unassigned.push(urgent);
            assignmentLog.unshift(\`[\${now()}] Urgent task queued — best candidate busy (no safe preempt)\`);
          }
        } else {
          autoAssignTask(urgent);
        }
      } else {
        unassigned.push(urgent);
      }
      renderAll();
    };

    document.getElementById('reset').onclick = ()=>{
      // quick demo reset: clear tasks & unassigned, restore workers
      tasks.splice(0,tasks.length);
      unassigned.splice(0,unassigned.length);
      assignmentLog.length = 0;
      workers.forEach(w=>{ w.available=true; w.currentTask=null; w.points = Math.round(Math.random()*120)+20;});
      renderAll();
    };

    /***** Rendering orchestration *****/
    function renderAll() {
      renderWorkers();
      renderUnassigned();
      renderActiveTasks();
      renderAssignmentLog();
      renderStatsAndCharts();
      renderWorkerPortal();
    }

    // initial render and small auto demo
    (function initDemo(){
      // seed a couple demo tasks
      autoAssignTask({ id: uuid('task'), title: 'CNC calibration', skills:['cnc','calibration'], duration: 3, createdAt: now() });
      autoAssignTask({ id: uuid('task'), title: 'Welding joint A', skills:['welding','safety'], duration: 2, createdAt: now() });
      autoAssignTask({ id: uuid('task'), title: 'Inspection batch', skills:['inspection'], duration: 1, createdAt: now() });

      // shuffle some points
      workers.forEach(w => w.points = Math.round(Math.random()*200)+10);

      // choose first worker in select
      setTimeout(()=> {
        renderAll();
        const sel = document.getElementById('select-worker');
        if(sel.options.length) sel.selectedIndex = 0;
        renderWorkerPortal();
      }, 50);
    })();
  </script>

  <footer class="text-center text-xs text-slate-500 p-4">
    Demo prototype — front-end only. Replace with backend + ML models for production.
  </footer>
</body>
</html>
