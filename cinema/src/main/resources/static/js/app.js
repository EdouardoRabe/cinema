// ========================================
// CINEMA - Main JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    initNavbar();
    initSeatSelection();
    initCategorySelection();
});

// Navbar scroll effect
function initNavbar() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
}

// Seat selection
function initSeatSelection() {
    const seats = document.querySelectorAll('.seat:not(.taken)');
    const selectedSeatsInput = document.getElementById('selectedSeats');
    const selectedCountEl = document.getElementById('selectedCount');
    const totalPriceEl = document.getElementById('totalPrice');
    
    if (!seats.length) return;

    // Si le modal de catégorie existe (page reservation front-office),
    // on ne fait RIEN ici - la gestion est faite dans le template
    const hasCategoryModal = document.getElementById('categoryModal');
    if (hasCategoryModal) {
        // Ne pas ajouter d'event listeners - le template gère tout
        return;
    }
    
    // legacy behavior: toggle selection directly (backoffice)
    seats.forEach(seat => {
        seat.addEventListener('click', function() {
            this.classList.toggle('selected');
            updateSeatSelection();
        });
    });

    function updateSeatSelection() {
        const selected = document.querySelectorAll('.seat.selected');
        const selectedIds = Array.from(selected).map(s => s.dataset.id);
        
        if (selectedSeatsInput) {
            selectedSeatsInput.value = selectedIds.join(',');
        }

        if (selectedCountEl) {
            selectedCountEl.textContent = selected.length;
        }

        // Calculate total price
        if (totalPriceEl) {
            let total = 0;
            selected.forEach(s => {
                total += parseFloat(s.dataset.price || 0);
            });
            totalPriceEl.textContent = total.toFixed(2).replace('.', ',') + ' Ar';
        }

        // Update summary
        updateSummary(selectedIds.length);
    }
}

// Category selection
function initCategorySelection() {
    const categories = document.querySelectorAll('.category-card');
    const categoryInput = document.getElementById('selectedCategory');
    
    if (!categories.length) return;

    categories.forEach(cat => {
        cat.addEventListener('click', function() {
            categories.forEach(c => c.classList.remove('selected'));
            this.classList.add('selected');
            
            if (categoryInput) {
                categoryInput.value = this.dataset.id;
            }

            // Update prices based on category
            updatePricesForCategory(this.dataset.id, this.dataset.price);
        });
    });
}

function updatePricesForCategory(categoryId, basePrice) {
    const seats = document.querySelectorAll('.seat:not(.taken)');
    seats.forEach(seat => {
        // Adjust price based on seat type and category
        const seatTypeMultiplier = seat.classList.contains('vip') ? 1.5 : 1;
        seat.dataset.price = (parseFloat(basePrice) * seatTypeMultiplier).toFixed(2);
    });

    // Recalculate if seats are selected
    const selected = document.querySelectorAll('.seat.selected');
    if (selected.length > 0) {
        const totalPriceEl = document.getElementById('totalPrice');
        if (totalPriceEl) {
            let total = 0;
            selected.forEach(s => {
                total += parseFloat(s.dataset.price || 0);
            });
            totalPriceEl.textContent = total.toFixed(2).replace('.', ',') + ' Ar';
        }
    }
}

function updateSummary(seatCount) {
    const summarySeats = document.getElementById('summarySeats');
    if (summarySeats) {
        summarySeats.textContent = seatCount + ' place(s)';
    }
}

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});

// Form validation helper
function validateReservationForm() {
    const selectedSeats = document.getElementById('selectedSeats');
    const selectedCategory = document.getElementById('selectedCategory');

    if (!selectedSeats || !selectedSeats.value) {
        alert('Veuillez sélectionner au moins une place.');
        return false;
    }

    if (!selectedCategory || !selectedCategory.value) {
        alert('Veuillez sélectionner une catégorie.');
        return false;
    }

    return true;
}
