const $ = id => document.getElementById(id);
let editingId = null;

// ---------- helpers ----------
function esc(v) {                     // prevents HTML injection (XSS)
  return String(v ?? '').replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function toast(text, ok = true) {
  const m = $('msg');
  m.textContent = text;
  m.className = ok ? 'ok' : 'err';
  m.style.display = 'block';
  clearTimeout(toast.t);
  toast.t = setTimeout(() => m.style.display = 'none', 3500);
}

async function api(url, method = 'GET', body) {
  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const msg = data && data.fields ? Object.values(data.fields).join(', ')
              : (data && data.error) || 'Request failed';
    throw new Error(msg);
  }
  return data;
}

// ---------- tabs ----------
document.querySelectorAll('nav button').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('nav button').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('section').forEach(s => s.classList.remove('active'));
    btn.classList.add('active');
    $(btn.dataset.tab).classList.add('active');
    refreshAll();
  });
});

// ---------- loaders ----------
async function loadDashboard() {
  const d = await api('/dashboard');
  $('totStudents').textContent = d.totalStudents;
  $('totCourses').textContent = d.totalCourses;
  $('totEnrollments').textContent = d.totalEnrollments;
  $('dashRows').innerHTML = d.courseWiseEnrollments.map(c =>
    `<tr><td>${esc(c.courseName)}</td><td>${esc(c.enrollments)}</td></tr>`).join('')
    || '<tr><td colspan="2">No courses yet</td></tr>';
}

async function loadSelectsAndCourses() {
  const [courses, students] = await Promise.all([api('/courses'), api('/students')]);

  $('courseRows').innerHTML = courses.map(c =>
    `<tr><td>${c.id}</td><td>${esc(c.name)}</td><td>${esc(c.duration)}</td></tr>`).join('')
    || '<tr><td colspan="3">No courses yet</td></tr>';

  const courseOpts = courses.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
  const prevCourse = $('sCourse').value;
  $('sCourse').innerHTML = '<option value="">-- Select course --</option>' + courseOpts;
  $('sCourse').value = prevCourse;
  $('eCourse').innerHTML = '<option value="">-- Select course --</option>' + courseOpts;

  $('eStudent').innerHTML = '<option value="">-- Select student --</option>' +
    students.map(s => `<option value="${s.id}">${esc(s.name)} (${esc(s.email)})</option>`).join('');
}

async function loadStudents() {
  const q = $('search').value.trim();
  const [students, enrollments] = await Promise.all([
    api('/students' + (q ? '?search=' + encodeURIComponent(q) : '')),
    api('/enrollments')
  ]);

  const courseMap = {};
  enrollments.forEach(e => (courseMap[e.student.id] ||= []).push(e.course.name));

  $('studentRows').innerHTML = students.map(s => `
    <tr>
      <td>${s.id}</td>
      <td>${esc(s.name)}</td>
      <td>${esc(s.email)}</td>
      <td>${esc(s.phone)}</td>
      <td>${esc(s.dateOfJoining)}</td>
      <td>${esc((courseMap[s.id] || []).join(', '))}</td>
      <td>
        <button class="small" onclick="editStudent(${s.id})">Edit</button>
        <button class="small danger" onclick="deleteStudent(${s.id})">Delete</button>
      </td>
    </tr>`).join('') || '<tr><td colspan="7">No students found</td></tr>';
}

async function loadEnrollments() {
  const list = await api('/enrollments');
  $('enrollRows').innerHTML = list.map(e => `
    <tr>
      <td>${e.id}</td>
      <td>${esc(e.student.name)}</td>
      <td>${esc(e.student.email)}</td>
      <td>${esc(e.course.name)}</td>
      <td>${esc(e.course.duration)}</td>
      <td>${esc(e.enrollmentDate)}</td>
    </tr>`).join('') || '<tr><td colspan="6">No enrollments yet</td></tr>';
}

async function refreshAll() {
  try {
    await Promise.all([loadDashboard(), loadSelectsAndCourses(), loadStudents(), loadEnrollments()]);
  } catch (e) {
    toast(e.message, false);
  }
}

// ---------- students ----------
function resetStudentForm() {
  editingId = null;
  $('studentForm').reset();
  $('sCourse').disabled = false;
  $('studentFormTitle').textContent = 'Add Student';
  $('studentSubmit').textContent = 'Add Student';
  $('studentCancel').style.display = 'none';
}

$('studentForm').addEventListener('submit', async ev => {
  ev.preventDefault();
  if (!$('studentForm').reportValidity()) return;
  const body = {
    name: $('sName').value.trim(),
    email: $('sEmail').value.trim(),
    phone: $('sPhone').value.trim(),
    dateOfJoining: $('sDate').value,
    courseId: $('sCourse').value ? Number($('sCourse').value) : null
  };
  try {
    if (editingId) {
      await api('/students/' + editingId, 'PUT', body);
      toast('Student updated');
    } else {
      await api('/students', 'POST', body);
      toast('Student added');
    }
    resetStudentForm();
    refreshAll();
  } catch (e) {
    toast(e.message, false);
  }
});

$('studentCancel').addEventListener('click', resetStudentForm);
$('search').addEventListener('input', () => loadStudents().catch(e => toast(e.message, false)));

async function editStudent(id) {
  try {
    const s = await api('/students/' + id);
    editingId = id;
    $('sName').value = s.name;
    $('sEmail').value = s.email;
    $('sPhone').value = s.phone;
    $('sDate').value = s.dateOfJoining;
    $('sCourse').value = '';
    $('sCourse').disabled = true;       // courses are managed from the Enrollments tab when editing
    $('studentFormTitle').textContent = 'Edit Student #' + id;
    $('studentSubmit').textContent = 'Update Student';
    $('studentCancel').style.display = 'inline-block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (e) {
    toast(e.message, false);
  }
}

async function deleteStudent(id) {
  if (!confirm('Delete this student? Their enrollments will also be removed.')) return;
  try {
    await api('/students/' + id, 'DELETE');
    toast('Student deleted');
    if (editingId === id) resetStudentForm();
    refreshAll();
  } catch (e) {
    toast(e.message, false);
  }
}

// ---------- courses ----------
$('courseForm').addEventListener('submit', async ev => {
  ev.preventDefault();
  if (!$('courseForm').reportValidity()) return;
  try {
    await api('/courses', 'POST', { name: $('cName').value.trim(), duration: $('cDuration').value.trim() });
    toast('Course added');
    $('courseForm').reset();
    refreshAll();
  } catch (e) {
    toast(e.message, false);
  }
});

// ---------- enrollments ----------
$('enrollForm').addEventListener('submit', async ev => {
  ev.preventDefault();
  if (!$('enrollForm').reportValidity()) return;
  try {
    await api('/enrollments', 'POST', {
      studentId: Number($('eStudent').value),
      courseId: Number($('eCourse').value)
    });
    toast('Enrolled successfully');
    refreshAll();
  } catch (e) {
    toast(e.message, false);     // shows "already enrolled" for duplicates
  }
});

refreshAll();