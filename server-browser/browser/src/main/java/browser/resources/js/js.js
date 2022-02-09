const rimeEnIn = ['hardi copain','vive le boudin','intrépide chafouin','petit chérubin', 'gros malin', 'petit vilain','sale coquin','vil faquin','crapuleux flandrin','vieux gredin','courageux libertin','vieux lutin','petit pantin','amis porcins','jeune radin','y\'aura l\'Romain','y\'aura l\'Jobin','vieux sagouin','fieffé taquin','et tout le tintouin','fichu zinzin','dis-le au voisin','tombe dans l\'ravin'];
const day = ['lundi','mardi','mercredi','jeudi','vendredi','samedi','dimanche'];
const title= document.getElementById('mainText');
const subTitle= document.getElementById('roundText');

document.addEventListener('click',changeText);

function changeText(){
 subTitle.innerHTML=rimeEnIn[Math.ceil(Math.random()*rimeEnIn.length-1)];  
 title.innerHTML=day[Math.ceil(Math.random()*day.length-1)]+' prochain';
 }
setInterval(changeText,2000);