var express = require('express');
var router = express.Router();
var parkController = require('../controllers/parkController.js');

/*
 * POST
 */
router.post('/addParks', parkController.addParks);

/*
 * GET
 */
router.get('/', parkController.getPark);


/*
 * GET
 */
router.get('/:id', parkController.show);

/*
 * PUT
 */
router.put('/:id', parkController.update);

/*
 * DELETE
 */
router.delete('/:id', parkController.remove);

module.exports = router;
