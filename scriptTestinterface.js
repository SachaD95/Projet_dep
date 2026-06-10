let mesPoints = [];
let isDrawing = false;
let typeModeleActuel = 'ligne';

// Éléments du DOM
const canvasModele = document.getElementById('canvasModele');
const ctxModele = canvasModele.getContext('2d');
const canvasDessin = document.getElementById('canvasDessin');
const ctxDessin = canvasDessin.getContext('2d');
const divResultats = document.getElementById('resultats');

// Options
const selectModele = document.getElementById('selectModele');
const inputTaille = document.getElementById('inputTaille');
const valTaille = document.getElementById('valTaille');
const inputAngle = document.getElementById('inputAngle');
const valAngle = document.getElementById('valAngle');
const chkAleatoire = document.getElementById('chkAleatoire');

// --- GESTION DE LA TAILLE DES CANVAS ---
function resizeCanvases() {
    canvasModele.width = canvasModele.clientWidth;
    canvasModele.height = canvasModele.clientHeight;
    canvasDessin.width = canvasDessin.clientWidth;
    canvasDessin.height = canvasDessin.clientHeight;
    chargerModele(); 
}
window.addEventListener('resize', resizeCanvases);

// --- ÉVÉNEMENTS DES CONTRÔLES ---
selectModele.addEventListener('change', (e) => {
    typeModeleActuel = e.target.value;
    chargerModele();
});

inputTaille.addEventListener('input', (e) => {
    valTaille.textContent = e.target.value;
    if (!chkAleatoire.checked) chargerModele();
});

inputAngle.addEventListener('input', (e) => {
    valAngle.textContent = e.target.value;
    if (!chkAleatoire.checked) chargerModele();
});

chkAleatoire.addEventListener('change', (e) => {
    inputTaille.disabled = e.target.checked;
    inputAngle.disabled = e.target.checked;
    if (e.target.checked) chargerModele();
});

// --- CHARGEMENT DU MODÈLE ---
function chargerModele() {
    let taille = inputTaille.value;
    let angle = inputAngle.value;

    if (chkAleatoire.checked) {
        taille = Math.floor(Math.random() * 400) + 100;
        angle = Math.floor(Math.random() * 360);
        inputTaille.value = taille;
        valTaille.textContent = taille;
        inputAngle.value = angle;
        valAngle.textContent = angle;
    }

    const url = `http://localhost:8080/api/modele/${typeModeleActuel}?taille=${taille}&angle=${angle}&largeurMax=${canvasModele.width}&hauteurMax=${canvasModele.height}`;

    fetch(url)
        .then(r => r.json())
        .then(modele => {
            dessinerModele(modele.points);
        })
        .catch(err => console.warn("Serveur non joignable pour le modèle.", err));
}

function dessinerModele(points) {
    ctxModele.clearRect(0, 0, canvasModele.width, canvasModele.height);
    if (!points || points.length === 0) return;

    ctxModele.beginPath();
    ctxModele.moveTo(points[0].x, points[0].y);
    for (let i = 1; i < points.length; i++) {
        ctxModele.lineTo(points[i].x, points[i].y);
    }
    ctxModele.strokeStyle = '#aaaaaa';
    ctxModele.lineWidth = 4;
    ctxModele.setLineDash([10, 8]);
    ctxModele.stroke();
    ctxModele.setLineDash([]);
}

// --- DESSIN UTILISATEUR ---
canvasDessin.addEventListener('pointerdown', (event) => {
    canvasDessin.setPointerCapture(event.pointerId);
    isDrawing = true;
    mesPoints = [];
    
    ctxDessin.clearRect(0, 0, canvasDessin.width, canvasDessin.height);
    divResultats.style.display = 'none';
    
    ctxDessin.beginPath();
    ctxDessin.strokeStyle = '#e74c3c';
    ctxDessin.lineWidth = 3;

    const rect = canvasDessin.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    ctxDessin.moveTo(x, y);
    mesPoints.push({ x: x, y: y });
});

canvasDessin.addEventListener('pointermove', (event) => {
    if (!isDrawing) return;

    const events = event.getCoalescedEvents ? event.getCoalescedEvents() : [event];
    const rect = canvasDessin.getBoundingClientRect();

    events.forEach(ev => {
        const x = ev.clientX - rect.left;
        const y = ev.clientY - rect.top;

        ctxDessin.lineTo(x, y);
        ctxDessin.stroke();
        mesPoints.push({ x: x, y: y }); 
    });
});

canvasDessin.addEventListener('pointerup', (event) => {
    isDrawing = false;
    canvasDessin.releasePointerCapture(event.pointerId);
    
    if (mesPoints.length > 5) {
        envoyerTrace();
    }
});

// --- COMMUNICATION AVEC LE SERVEUR ---
function envoyerTrace() {
    // Récupération des options cochées (avec fallback si la checkbox n'existe pas encore)
    const optionsChoisies = {
        taille: document.getElementById('chkScoreTaille') ? document.getElementById('chkScoreTaille').checked : true,
        angle: document.getElementById('chkScoreAngle').checked,
        tremblement: document.getElementById('chkScoreTremblement').checked,
        vitesse: document.getElementById('chkScoreVitesse').checked
    };

    const colis = {
        points: mesPoints,
        options: optionsChoisies,
        typeModele: typeModeleActuel,
        taille: parseFloat(inputTaille.value),     
        angle: parseFloat(inputAngle.value),       
        largeurMax: canvasModele.width,            
        hauteurMax: canvasModele.height            
    };

    fetch('http://localhost:8080/api/calculer-score', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(colis)
    })
    .then(r => r.json())
    .then(donnees => {
        afficherResultats(donnees);

        if (chkAleatoire.checked) {
            setTimeout(chargerModele, 500); 
        }
    })
    .catch(err => {
        console.error(err);
        divResultats.style.display = 'block';
        divResultats.innerHTML = "<p style='color: red;'>Erreur serveur.</p>";
    });
}

function afficherResultats(donnees) {
    divResultats.style.display = 'block';
    
    let affichage = `<h3>Analyse du tracé :</h3>`;
    
    if (donnees.score !== undefined)
        affichage += `<p><strong>Score Global :</strong> ${donnees.score} / 100</p>`;
    
    // 🌟 NOUVEAU : Affichage du score de Taille
    if (donnees.commTaille) {
        const partiesTaille = donnees.commTaille.split('|');
        const scoreTaille = partiesTaille[0];
        const detailsTaille = partiesTaille[1] || "";
        affichage += `<p style="margin-top: 10px;"> <strong>Précision de Taille :</strong> ${scoreTaille} / 100 <span style="font-size: 0.85rem; color: #6b7280;">(${detailsTaille})</span></p>`;
    }

    if (donnees.commAngle)
        affichage += `<p style="margin-top: 10px;"> <strong>Précision d'Angle :</strong> ${donnees.commAngle}</p>`;
    
    if (donnees.commVitesse) {
        const parties = donnees.commVitesse.split('|'); 
        const symboles = parties[0].trim().split(' ');
        

        const configVitesse = {
            '+': { color: '#ef4444', label: 'Rapide', icon: '🚀' },
            '-': { color: '#3b82f6', label: 'Lent', icon: '🐢' },
            '~': { color: '#10b981', label: 'Stable', icon: '⚖️' }
        };

        const badges = symboles.map(s => {
            const config = configVitesse[s] || { color: '#9ca3af', label: '?', icon: '' };
            return `<span title="${config.label}" style="background-color: ${config.color}; color: white; padding: 4px 8px; border-radius: 6px; font-size: 0.85rem; font-weight: bold; margin-right: 6px; display: inline-flex; align-items: center; gap: 4px;">${config.icon} ${s}</span>`;
        }).join('');

        affichage += `<div style="margin-top: 10px; display: flex; align-items: center; flex-wrap: wrap;"><strong>Régularité de la vitesse :</strong> &nbsp; ${badges}</div>`;
        
    }

    if (donnees.commTremblement)
        affichage += `<p style="margin-top: 10px;">〰️ <strong>Tremblements :</strong> ${donnees.commTremblement}</p>`;

    divResultats.innerHTML = affichage;
}

// Initialisation au démarrage
setTimeout(resizeCanvases, 100);