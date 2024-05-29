var express = require('express');
var router = express.Router();
var pollenController = require('../controllers/pollenController.js');

/*
 * GET
 */
router.get('/', pollenController.list);

/*
 * GET
 */
router.get('/:id', pollenController.show);

/*
 * POST
 */
router.post('/', pollenController.create);

/*
 * PUT
 */
router.put('/:id', pollenController.update);

/*
 * DELETE
 */
router.delete('/:id', pollenController.remove);

module.exports = router;
