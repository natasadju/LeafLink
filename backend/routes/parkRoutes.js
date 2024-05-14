var express = require('express');
var router = express.Router();
var parkController = require('../controllers/parkController.js');

/*
 * GET
 */
router.get('/', parkController.list);

/*
 * GET
 */
router.get('/:id', parkController.show);

/*
 * POST
 */
router.post('/', parkController.create);

/*
 * PUT
 */
router.put('/:id', parkController.update);

/*
 * DELETE
 */
router.delete('/:id', parkController.remove);

module.exports = router;
